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
 * Part 4: JPA entities, a composite primary key and Repository queries.
 * DataJpaTest starts only the database parts: an in-memory H2 database + Flyway migrations + JPA.
 * Each test method is rolled back afterwards, so tests do not affect each other's data.
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

    /** Writes pending changes to the database and clears the first-level cache, so later queries really hit the database. */
    void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("4.1 A StationEntity saved and read back has the same fields")
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
    @DisplayName("4.2 AvailabilityEntity is stored and loaded by the (number, lastUpdate) composite key")
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
    @DisplayName("4.2 Two saves with the same key leave one row (the second is an update)")
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
    @DisplayName("4.3 findOpenStations returns only OPEN stations, ordered by name")
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
    @DisplayName("4.4 findLatest returns a station's latest row, or Optional.empty() if there is none")
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
    @DisplayName("4.5 findLatestForAllStations returns only the latest row per station")
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
