package ie.dublinbikes.station;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import ie.dublinbikes.station.domain.NearbyStation;
import ie.dublinbikes.station.domain.Station;
import ie.dublinbikes.station.domain.StationFinder;
import ie.dublinbikes.station.persistence.AvailabilityEntity;
import ie.dublinbikes.station.persistence.AvailabilityId;
import ie.dublinbikes.station.persistence.AvailabilityRepository;
import ie.dublinbikes.station.persistence.StationEntity;
import ie.dublinbikes.station.persistence.StationRepository;
import jakarta.persistence.EntityManager;

/** Part 6.1: the Service layer combines station info and the latest availability into the Station domain object. */
@DataJpaTest
@Import({StationQueryService.class, StationFinder.class})
class Part6QueryServiceTest {

    static final Instant T1 = Instant.parse("2026-10-25T08:00:00Z");
    static final Instant T2 = Instant.parse("2026-10-25T08:05:00Z");

    @Autowired
    StationQueryService queryService;

    @Autowired
    StationRepository stationRepository;

    @Autowired
    AvailabilityRepository availabilityRepository;

    @Autowired
    EntityManager entityManager;

    static StationEntity station(int number, String name, double lat, double lng, int bikeStands, String status) {
        StationEntity s = new StationEntity(number);
        s.updateDetails(name, name + " address", lat, lng, false, bikeStands, status);
        return s;
    }

    @BeforeEach
    void seed() {
        stationRepository.saveAll(List.of(
                station(42, "SMITHFIELD NORTH", 53.349562, -6.278198, 30, "OPEN"),
                station(33, "PRINCES STREET", 53.349013, -6.260311, 20, "OPEN"),
                station(5, "CHARLEMONT PLACE", 53.330662, -6.260177, 40, "OPEN")));
        availabilityRepository.saveAll(List.of(
                new AvailabilityEntity(new AvailabilityId(42, T1), 1, 29, "OPEN"),
                new AvailabilityEntity(new AvailabilityId(42, T2), 3, 27, "OPEN"),
                new AvailabilityEntity(new AvailabilityId(33, T2), 0, 20, "OPEN")));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("listStations: ordered by station number, using the latest availability; stations with no availability record have 0 bikes and stands")
    void listStations() {
        List<Station> stations = queryService.listStations();

        assertThat(stations).extracting(Station::number).containsExactly(5, 33, 42);
        assertThat(stations.get(2)).isEqualTo(new Station(42, "SMITHFIELD NORTH", "SMITHFIELD NORTH address",
                53.349562, -6.278198, "OPEN", 3, 27, 30));
        assertThat(stations.get(0).availableBikes()).isZero();
        assertThat(stations.get(0).availableStands()).isZero();
        assertThat(stations.get(0).totalStands()).isEqualTo(40);
    }

    @Test
    @DisplayName("getStation: returns a single station")
    void getStation() {
        assertThat(queryService.getStation(42).availableBikes()).isEqualTo(3);
    }

    @Test
    @DisplayName("getStation: throws StationNotFoundException when the station does not exist")
    void getStationNotFound() {
        assertThatThrownBy(() -> queryService.getStation(999))
                .isInstanceOf(StationNotFoundException.class)
                .hasMessage("Station 999 does not exist");
    }

    @Test
    @DisplayName("findNearest: reuses the Part 1 StationFinder; with requireBikes, stations without bikes are excluded")
    void findNearest() {
        List<NearbyStation> all = queryService.findNearest(53.3498, -6.2603, 2, false);
        assertThat(all).extracting(n -> n.station().number()).containsExactly(33, 42);

        List<NearbyStation> withBikes = queryService.findNearest(53.3498, -6.2603, 2, true);
        assertThat(withBikes).extracting(n -> n.station().number()).containsExactly(42);
    }
}
