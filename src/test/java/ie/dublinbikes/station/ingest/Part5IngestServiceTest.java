package ie.dublinbikes.station.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import ie.dublinbikes.jcdecaux.JcDecauxStationDto;
import ie.dublinbikes.station.persistence.AvailabilityId;
import ie.dublinbikes.station.persistence.AvailabilityRepository;
import ie.dublinbikes.station.persistence.StationRepository;
import jakarta.persistence.EntityManager;

/** Part 5.1：把抓到的数据写进数据库（站点 upsert，可用量幂等插入）。 */
@DataJpaTest
@Import(StationIngestService.class)
class Part5IngestServiceTest {

    static final long T1 = 1761400800000L;
    static final long T2 = 1761401100000L;

    @Autowired
    StationIngestService ingestService;

    @Autowired
    StationRepository stationRepository;

    @Autowired
    AvailabilityRepository availabilityRepository;

    @Autowired
    EntityManager entityManager;

    static JcDecauxStationDto dto(int number, String name, int bikes, int stands, String status, Long lastUpdate) {
        return new JcDecauxStationDto(number, "dublin", name, name + " address",
                new JcDecauxStationDto.Position(53.35, -6.26), true, false, bikes + stands, stands, bikes, status, lastUpdate);
    }

    static final List<JcDecauxStationDto> FIRST_SCRAPE = List.of(
            dto(42, "SMITHFIELD NORTH", 3, 27, "OPEN", T1),
            dto(2, "BLESSINGTON STREET", 7, 13, "CLOSED", T1),
            dto(5, "CHARLEMONT PLACE", 10, 30, "OPEN", T1));

    void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Example：第一次抓取，3 个站点、3 条可用量记录全部写入")
    void firstIngest() {
        IngestResult result = ingestService.ingest(FIRST_SCRAPE);
        flushAndClear();

        assertThat(result).isEqualTo(new IngestResult(3, 3));
        assertThat(stationRepository.count()).isEqualTo(3);
        assertThat(availabilityRepository.count()).isEqualTo(3);
        var smithfield = availabilityRepository.findById(new AvailabilityId(42, Instant.ofEpochMilli(T1))).orElseThrow();
        assertThat(smithfield.getAvailableBikes()).isEqualTo(3);
        assertThat(smithfield.getAvailableBikeStands()).isEqualTo(27);
    }

    @Test
    @DisplayName("幂等：同样的数据再写一次，不会重复插入可用量记录")
    void idempotent() {
        ingestService.ingest(FIRST_SCRAPE);
        flushAndClear();

        IngestResult second = ingestService.ingest(FIRST_SCRAPE);
        flushAndClear();

        assertThat(second).isEqualTo(new IngestResult(3, 0));
        assertThat(stationRepository.count()).isEqualTo(3);
        assertThat(availabilityRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("upsert：站点信息变化时更新原来那一行；last_update 变了就新插一条可用量")
    void updatesStationAndAddsAvailability() {
        ingestService.ingest(FIRST_SCRAPE);
        flushAndClear();

        IngestResult result = ingestService.ingest(List.of(dto(42, "SMITHFIELD NORTH (RENAMED)", 1, 29, "OPEN", T2)));
        flushAndClear();

        assertThat(result).isEqualTo(new IngestResult(1, 1));
        assertThat(stationRepository.count()).isEqualTo(3);
        assertThat(stationRepository.findById(42).orElseThrow().getName()).isEqualTo("SMITHFIELD NORTH (RENAMED)");
        assertThat(availabilityRepository.findLatest(42).orElseThrow().getAvailableBikes()).isEqualTo(1);
        assertThat(availabilityRepository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("last_update 为 null 时只保存站点，不插入可用量")
    void nullLastUpdate() {
        IngestResult result = ingestService.ingest(List.of(dto(33, "PRINCES STREET", 0, 20, "OPEN", null)));
        flushAndClear();

        assertThat(result).isEqualTo(new IngestResult(1, 0));
        assertThat(stationRepository.existsById(33)).isTrue();
        assertThat(availabilityRepository.count()).isZero();
    }

    @Test
    @DisplayName("空列表：什么都不写")
    void emptyInput() {
        assertThat(ingestService.ingest(List.of())).isEqualTo(new IngestResult(0, 0));
    }
}
