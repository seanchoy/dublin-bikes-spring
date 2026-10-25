package ie.dublinbikes.station.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import jakarta.persistence.EntityManager;

/**
 * Part 4：JPA 实体、复合主键、Repository 查询。
 * DataJpaTest 只启动数据库相关的部分：一个内存 H2 数据库 + Flyway 建表 + JPA。
 * 每个测试方法结束后会自动回滚，所以测试之间的数据互不影响。
 */
@DataJpaTest
class Part4PersistenceTest {

    static final Instant T1 = Instant.parse("2026-10-25T08:00:00Z");
    static final Instant T2 = Instant.parse("2026-10-25T08:05:00Z");
    static final Instant T3 = Instant.parse("2026-10-25T08:10:00Z");

    @Autowired
    StationRepository stationRepository;

    @Autowired
    AvailabilityRepository availabilityRepository;

    @Autowired
    EntityManager entityManager;

    static StationEntity station(int number, String name, String status) {
        StationEntity s = new StationEntity(number);
        s.updateDetails(name, name + " address", 53.35, -6.26, true, 30, status);
        return s;
    }

    /** 把还没写进数据库的修改写进去，再清空一级缓存，这样后面的查询一定会真的去查数据库。 */
    void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("4.1 StationEntity 存进去再读出来，所有字段都一样")
    void stationRoundTrip() {
        stationRepository.save(station(42, "SMITHFIELD NORTH", "OPEN"));
        flushAndClear();

        StationEntity loaded = stationRepository.findById(42).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("SMITHFIELD NORTH");
        assertThat(loaded.getAddress()).isEqualTo("SMITHFIELD NORTH address");
        assertThat(loaded.getLat()).isEqualTo(53.35);
        assertThat(loaded.getLng()).isEqualTo(-6.26);
        assertThat(loaded.isBanking()).isTrue();
        assertThat(loaded.getBikeStands()).isEqualTo(30);
        assertThat(loaded.getStatus()).isEqualTo("OPEN");
    }

    @Test
    @DisplayName("4.2 AvailabilityEntity 用 (number, lastUpdate) 复合主键存取")
    void availabilityCompositeKey() {
        stationRepository.save(station(42, "SMITHFIELD NORTH", "OPEN"));
        availabilityRepository.save(new AvailabilityEntity(new AvailabilityId(42, T1), 3, 27, "OPEN"));
        availabilityRepository.save(new AvailabilityEntity(new AvailabilityId(42, T2), 5, 25, "OPEN"));
        flushAndClear();

        AvailabilityEntity loaded = availabilityRepository.findById(new AvailabilityId(42, T2)).orElseThrow();
        assertThat(loaded.getAvailableBikes()).isEqualTo(5);
        assertThat(loaded.getAvailableBikeStands()).isEqualTo(25);
        assertThat(loaded.getId().lastUpdate()).isEqualTo(T2);
        assertThat(availabilityRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("4.2 主键相同的两次 save 只会有一行（第二次是更新）")
    void sameKeyIsUpdate() {
        stationRepository.save(station(42, "SMITHFIELD NORTH", "OPEN"));
        availabilityRepository.save(new AvailabilityEntity(new AvailabilityId(42, T1), 3, 27, "OPEN"));
        flushAndClear();
        availabilityRepository.save(new AvailabilityEntity(new AvailabilityId(42, T1), 4, 26, "OPEN"));
        flushAndClear();

        assertThat(availabilityRepository.count()).isEqualTo(1);
        assertThat(availabilityRepository.findById(new AvailabilityId(42, T1)).orElseThrow().getAvailableBikes())
                .isEqualTo(4);
    }

    @Test
    @DisplayName("4.3 findOpenStations 只返回 OPEN 的站点，按名字排序")
    void findOpenStations() {
        stationRepository.saveAll(List.of(
                station(1, "CHARLEMONT PLACE", "OPEN"),
                station(2, "BLESSINGTON STREET", "CLOSED"),
                station(3, "ANNESLEY BRIDGE", "OPEN")));
        flushAndClear();

        assertThat(stationRepository.findOpenStations())
                .extracting(StationEntity::getName)
                .containsExactly("ANNESLEY BRIDGE", "CHARLEMONT PLACE");
    }

    @Test
    @DisplayName("4.4 findLatest 返回某个站点最新的一条记录；没有记录时返回 Optional.empty()")
    void findLatest() {
        stationRepository.saveAll(List.of(station(42, "SMITHFIELD NORTH", "OPEN"), station(5, "CHARLEMONT PLACE", "OPEN")));
        availabilityRepository.saveAll(List.of(
                new AvailabilityEntity(new AvailabilityId(42, T2), 5, 25, "OPEN"),
                new AvailabilityEntity(new AvailabilityId(42, T3), 7, 23, "OPEN"),
                new AvailabilityEntity(new AvailabilityId(42, T1), 3, 27, "OPEN")));
        flushAndClear();

        assertThat(availabilityRepository.findLatest(42))
                .get()
                .extracting(AvailabilityEntity::getAvailableBikes)
                .isEqualTo(7);
        assertThat(availabilityRepository.findLatest(5)).isEmpty();
    }

    @Test
    @DisplayName("4.5 findLatestForAllStations 每个站点只返回最新的一条")
    void findLatestForAllStations() {
        stationRepository.saveAll(List.of(
                station(42, "SMITHFIELD NORTH", "OPEN"),
                station(5, "CHARLEMONT PLACE", "OPEN"),
                station(2, "BLESSINGTON STREET", "OPEN")));
        availabilityRepository.saveAll(List.of(
                new AvailabilityEntity(new AvailabilityId(42, T1), 3, 27, "OPEN"),
                new AvailabilityEntity(new AvailabilityId(42, T3), 7, 23, "OPEN"),
                new AvailabilityEntity(new AvailabilityId(5, T2), 10, 30, "OPEN"),
                new AvailabilityEntity(new AvailabilityId(5, T1), 12, 28, "OPEN")));
        flushAndClear();

        List<AvailabilityEntity> latest = availabilityRepository.findLatestForAllStations();

        assertThat(latest).hasSize(2);
        assertThat(latest)
                .extracting(a -> a.getId().number() + "@" + a.getId().lastUpdate())
                .containsExactlyInAnyOrder("42@" + T3, "5@" + T2);
    }
}
