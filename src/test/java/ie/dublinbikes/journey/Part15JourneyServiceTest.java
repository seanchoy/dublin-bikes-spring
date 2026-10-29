package ie.dublinbikes.journey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ie.dublinbikes.journey.JourneyException.Reason;
import ie.dublinbikes.journey.google.GoogleMapsService;
import ie.dublinbikes.station.StationQueryService;
import ie.dublinbikes.station.domain.Station;
import ie.dublinbikes.station.domain.StationFinder;

/** Part 15.1: planning a journey (Google is mocked; StationFinder is the real Part 1 implementation). */
class Part15JourneyServiceTest {

    static final LatLng SPIRE = new LatLng(53.3498, -6.2603);
    static final LatLng CANAL = new LatLng(53.3310, -6.2600);

    static final Station PRINCES = new Station(33, "PRINCES STREET", "Princes Street", 53.349013, -6.260311, "OPEN", 0, 20, 20);
    static final Station DAME = new Station(10, "DAME STREET", "Dame Street", 53.344007, -6.266802, "OPEN", 5, 0, 16);
    static final Station CHARLEMONT = new Station(5, "CHARLEMONT PLACE", "Charlemont Place", 53.330662, -6.260177, "OPEN", 10, 30, 40);
    static final Station HARCOURT = new Station(23, "HARCOURT TERRACE", "Harcourt Terrace", 53.332763, -6.257942, "OPEN", 2, 0, 20);

    static RouteLeg leg(TravelMode mode, long meters, long seconds) {
        return new RouteLeg(mode, meters, seconds, meters + " m", seconds + " s", "poly");
    }

    final GoogleMapsService maps = mock(GoogleMapsService.class);
    final StationQueryService stations = mock(StationQueryService.class);
    final JourneyService service = new JourneyService(maps, stations, new StationFinder());

    @Test
    @DisplayName("Example: pickup is the nearest station with bikes near the start, dropoff the nearest station with free stands near the end")
    void plansJourney() {
        when(maps.geocode("O'Connell Street")).thenReturn(SPIRE);
        when(maps.geocode("Grand Canal")).thenReturn(CANAL);
        when(stations.listStations()).thenReturn(List.of(PRINCES, DAME, CHARLEMONT, HARCOURT));
        LatLng dameAt = new LatLng(DAME.lat(), DAME.lng());
        LatLng charlemontAt = new LatLng(CHARLEMONT.lat(), CHARLEMONT.lng());
        when(maps.directions(SPIRE, dameAt, TravelMode.WALKING)).thenReturn(leg(TravelMode.WALKING, 800, 600));
        when(maps.directions(dameAt, charlemontAt, TravelMode.BICYCLING)).thenReturn(leg(TravelMode.BICYCLING, 1700, 400));
        when(maps.directions(charlemontAt, CANAL, TravelMode.WALKING)).thenReturn(leg(TravelMode.WALKING, 100, 80));

        JourneyPlan plan = service.plan("O'Connell Street", "Grand Canal");

        assertThat(plan.pickup().station().number()).isEqualTo(10);     // 33 is closer but has no bikes
        assertThat(plan.dropoff().station().number()).isEqualTo(5);     // is 23 closer? it has no free stands
        assertThat(plan.start()).isEqualTo(SPIRE);
        assertThat(plan.end()).isEqualTo(CANAL);
        assertThat(plan.cycle().distanceMeters()).isEqualTo(1700);
        assertThat(plan.totalDistanceMeters()).isEqualTo(2600);
        assertThat(plan.totalDurationSeconds()).isEqualTo(1080);
    }

    @Test
    @DisplayName("No station with bikes nearby: JourneyException(NO_STATION_AVAILABLE), Directions is not called")
    void noBikesAnywhere() {
        when(maps.geocode(any())).thenReturn(SPIRE);
        when(stations.listStations()).thenReturn(List.of(PRINCES));

        assertThatThrownBy(() -> service.plan("A", "B"))
                .isInstanceOfSatisfying(JourneyException.class,
                        e -> assertThat(e.getReason()).isEqualTo(Reason.NO_STATION_AVAILABLE));
        verify(maps, never()).directions(any(), any(), any());
    }

    @Test
    @DisplayName("Address not found: the exception from GoogleMapsService propagates unchanged")
    void addressNotFound() {
        when(maps.geocode("Nowhere")).thenThrow(new JourneyException(Reason.ADDRESS_NOT_FOUND, "Could not find address: Nowhere"));

        assertThatThrownBy(() -> service.plan("Nowhere", "Grand Canal"))
                .isInstanceOfSatisfying(JourneyException.class,
                        e -> assertThat(e.getReason()).isEqualTo(Reason.ADDRESS_NOT_FOUND));
    }
}
