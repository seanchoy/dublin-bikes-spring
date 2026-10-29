package ie.dublinbikes.journey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ie.dublinbikes.forecast.HourlyPrediction;
import ie.dublinbikes.forecast.StationForecast;
import ie.dublinbikes.forecast.StationForecastService;
import ie.dublinbikes.journey.JourneyException.Reason;
import ie.dublinbikes.journey.google.GoogleMapsService;
import ie.dublinbikes.station.StationQueryService;
import ie.dublinbikes.station.domain.Station;
import ie.dublinbikes.station.domain.StationFinder;

/** Predictive journey: choosing stations from the forecast (Google, stations and forecasts are mocked; StationFinder is real). */
class Part15PredictiveJourneyServiceTest {

    static final LatLng SPIRE = new LatLng(53.3498, -6.2603);
    static final LatLng CANAL = new LatLng(53.3310, -6.2600);

    // The same four stations as Part15JourneyServiceTest, plus a closed station right next to the start
    static final Station PRINCES = new Station(33, "PRINCES STREET", "Princes Street", 53.349013, -6.260311, "OPEN", 0, 20, 20);
    static final Station DAME = new Station(10, "DAME STREET", "Dame Street", 53.344007, -6.266802, "OPEN", 5, 0, 16);
    static final Station CHARLEMONT = new Station(5, "CHARLEMONT PLACE", "Charlemont Place", 53.330662, -6.260177, "OPEN", 10, 30, 40);
    static final Station HARCOURT = new Station(23, "HARCOURT TERRACE", "Harcourt Terrace", 53.332763, -6.257942, "OPEN", 2, 0, 20);
    static final Station CLOSED = new Station(99, "CLOSED STATION", "Spire", 53.3498, -6.2603, "CLOSED", 10, 10, 20);

    // Now is 2026-10-28 17:20 (Dublin is on UTC at this time of year)
    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-28T17:20:00Z"), ZoneId.of("Europe/Dublin"));
    static final LocalDateTime DEPART = LocalDateTime.parse("2026-10-29T08:00");
    static final int HOURS = 16;    // 17:00 to 08:00 the next day is 15 hours, so 16 points reach 08:00

    static final RouteLeg WALK = new RouteLeg(TravelMode.WALKING, 800, 600, "0.8 km", "10 mins", "w");
    static final RouteLeg CYCLE = new RouteLeg(TravelMode.BICYCLING, 1700, 400, "1.7 km", "7 mins", "c");

    final GoogleMapsService maps = mock(GoogleMapsService.class);
    final StationQueryService stations = mock(StationQueryService.class);
    final StationForecastService forecasts = mock(StationForecastService.class);
    final PredictiveJourneyService service =
            new PredictiveJourneyService(maps, stations, new StationFinder(), forecasts, CLOCK);

    @BeforeEach
    void setUp() {
        when(maps.geocode("O'Connell Street")).thenReturn(SPIRE);
        when(maps.geocode("Grand Canal")).thenReturn(CANAL);
        when(stations.listStations()).thenReturn(List.of(CLOSED, PRINCES, DAME, CHARLEMONT, HARCOURT));
        when(maps.directions(any(), any(), eq(TravelMode.WALKING))).thenReturn(WALK);
        when(maps.directions(any(), any(), eq(TravelMode.BICYCLING))).thenReturn(CYCLE);
    }

    /** Predictions for 07:00, 08:00 and 09:00; only the 08:00 point uses the given bikes and docks. */
    static StationForecast predicted(Station s, double bikesAt8, double docksAt8) {
        return new StationForecast(s.number(), s.name(), s.totalStands(), s.availableBikes(), false, List.of(
                prediction("2026-10-29T07:00", 0, 0),
                prediction("2026-10-29T08:00", bikesAt8, docksAt8),
                prediction("2026-10-29T09:00", 0, 0)));
    }

    static StationForecast unavailable(Station s) {
        return new StationForecast(s.number(), s.name(), s.totalStands(), s.availableBikes(), true, List.of());
    }

    static HourlyPrediction prediction(String time, double bikes, double docks) {
        return new HourlyPrediction(LocalDateTime.parse(time), bikes, docks, 8.0, 80.0, 1013.0, 0);
    }

    @Test
    @DisplayName("Chooses stations from the 08:00 forecast: 33 has no bikes now but 6 expected; dropoff skips 5, which is expected to be full")
    void picksStationsByPrediction() {
        when(forecasts.forecast(33, HOURS)).thenReturn(predicted(PRINCES, 6.2, 13.8));
        when(forecasts.forecast(5, HOURS)).thenReturn(predicted(CHARLEMONT, 39.6, 0.4));
        when(forecasts.forecast(23, HOURS)).thenReturn(predicted(HARCOURT, 11.0, 9.0));

        PredictedJourneyPlan plan = service.plan("O'Connell Street", "Grand Canal", DEPART);

        assertThat(plan.journey().pickup().station().number()).isEqualTo(33);
        assertThat(plan.pickupOutlook()).isEqualTo(new StationOutlook(DEPART, 6.2, 13.8, true));
        assertThat(plan.journey().dropoff().station().number()).isEqualTo(23);
        assertThat(plan.dropoffOutlook().docks()).isEqualTo(9.0);
        assertThat(plan.departAt()).isEqualTo(DEPART);
        assertThat(plan.journey().totalDistanceMeters()).isEqualTo(800 + 1700 + 800);

        verify(forecasts, never()).forecast(eq(99), anyInt());   // closed stations are not considered
        verify(forecasts, never()).forecast(eq(10), anyInt());   // 33 already qualifies, so 10 is never forecast
    }

    @Test
    @DisplayName("Forecast unavailable: falls back to real-time numbers and marks predicted = false")
    void fallsBackToRealTimeWhenForecastUnavailable() {
        when(forecasts.forecast(anyInt(), anyInt())).thenAnswer(inv -> {
            int number = inv.getArgument(0);
            Station s = List.of(PRINCES, DAME, CHARLEMONT, HARCOURT).stream()
                    .filter(st -> st.number() == number).findFirst().orElseThrow();
            return unavailable(s);
        });

        PredictedJourneyPlan plan = service.plan("O'Connell Street", "Grand Canal", DEPART);

        assertThat(plan.journey().pickup().station().number()).isEqualTo(10);    // 33 has no bikes now
        assertThat(plan.pickupOutlook()).isEqualTo(new StationOutlook(null, 5, 0, false));
        assertThat(plan.journey().dropoff().station().number()).isEqualTo(5);    // 5 has 30 free stands now
        assertThat(plan.dropoffOutlook().predicted()).isFalse();
    }

    @Test
    @DisplayName("No candidate is expected to have bikes: NO_STATION_AVAILABLE")
    void noStationExpectedToHaveBikes() {
        when(forecasts.forecast(anyInt(), anyInt())).thenReturn(predicted(PRINCES, 0.3, 19.7));

        assertThatThrownBy(() -> service.plan("O'Connell Street", "Grand Canal", DEPART))
                .isInstanceOfSatisfying(JourneyException.class,
                        e -> assertThat(e.getReason()).isEqualTo(Reason.NO_STATION_AVAILABLE));
    }

    @Test
    @DisplayName("Departure in the past or more than 48 hours ahead: InvalidDepartureException, Google is not called")
    void rejectsDepartureOutsideForecastRange() {
        assertThatThrownBy(() -> service.plan("O'Connell Street", "Grand Canal", LocalDateTime.parse("2026-10-28T17:00")))
                .isInstanceOf(InvalidDepartureException.class);
        assertThatThrownBy(() -> service.plan("O'Connell Street", "Grand Canal", LocalDateTime.parse("2026-10-30T18:00")))
                .isInstanceOf(InvalidDepartureException.class);
        verify(maps, never()).geocode(any());
    }

    @Test
    @DisplayName("hoursToCover: how many hourly points from the current hour reach the target time (at most 48)")
    void hoursToCover() {
        LocalDateTime now = LocalDateTime.parse("2026-10-28T17:20");
        assertThat(PredictiveJourneyService.hoursToCover(now, LocalDateTime.parse("2026-10-28T19:40"))).isEqualTo(3);
        assertThat(PredictiveJourneyService.hoursToCover(now, LocalDateTime.parse("2026-10-28T17:30"))).isEqualTo(1);
        assertThat(PredictiveJourneyService.hoursToCover(now, DEPART)).isEqualTo(HOURS);
        assertThat(PredictiveJourneyService.hoursToCover(now, now.plusHours(48))).isEqualTo(48);
    }
}
