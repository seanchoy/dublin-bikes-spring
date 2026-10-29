package ie.dublinbikes.forecast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import ie.dublinbikes.weather.WeatherQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import ie.dublinbikes.station.StationNotFoundException;
import ie.dublinbikes.station.StationQueryService;
import ie.dublinbikes.station.domain.Station;

/**
 * Part 9.1: the forecast service.
 * Clock.fixed pins "now" to one moment, so "the next full hour" is deterministic (Part 2, think question 3).
 */
class Part9ForecastServiceTest {

    static final ZoneId DUBLIN = ZoneId.of("Europe/Dublin");
    static final Station SMITHFIELD = new Station(42, "SMITHFIELD NORTH", "Smithfield North",
            53.349562, -6.278198, "OPEN", 3, 27, 30);
    static final List<HourlyPrediction> PREDICTIONS = List.of(
            new HourlyPrediction(LocalDateTime.parse("2026-10-28T18:00:00"), 12.4, 17.6, 8.0, 83.5, 1014.5, 0));

    final StationQueryService stations = mock(StationQueryService.class);
    final ResilientForecastClient forecastClient = mock(ResilientForecastClient.class);
    final WeatherQueryService weather = mock(WeatherQueryService.class);

    StationForecastService serviceAt(String instant) {
        return new StationForecastService(stations, forecastClient, Clock.fixed(Instant.parse(instant), DUBLIN), weather);
    }

    static List<LocalDateTime> times(String... values) {
        return java.util.Arrays.stream(values).map(LocalDateTime::parse).toList();
    }

    @Test
    @DisplayName("9.1a Now is 17:20: start at the next full hour, 18:00, one per hour")
    void timesStartAtNextHour() {
        assertThat(serviceAt("2026-10-28T17:20:00Z").forecastTimes(3))
                .containsExactlyElementsOf(times("2026-10-28T18:00", "2026-10-28T19:00", "2026-10-28T20:00"));
    }

    @Test
    @DisplayName("9.1a Now is exactly 18:00: start at 18:00")
    void timesOnTheHour() {
        assertThat(serviceAt("2026-10-28T18:00:00Z").forecastTimes(2))
                .containsExactlyElementsOf(times("2026-10-28T18:00", "2026-10-28T19:00"));
    }

    @Test
    @DisplayName("9.1a Times stay continuous across midnight")
    void timesAcrossMidnight() {
        assertThat(serviceAt("2026-10-28T22:59:59Z").forecastTimes(3))
                .containsExactlyElementsOf(times("2026-10-28T23:00", "2026-10-29T00:00", "2026-10-29T01:00"));
    }

    @Test
    @DisplayName("9.1b Normal case: the request has the station number, total stands and 8 time points, no weather")
    void buildsRequestAndReturnsForecast() {
        when(stations.getStation(42)).thenReturn(SMITHFIELD);
        when(forecastClient.predict(any())).thenReturn(Optional.of(PREDICTIONS));

        StationForecast result = serviceAt("2026-10-28T17:20:00Z").forecast(42, 8);

        ArgumentCaptor<ForecastRequest> captor = ArgumentCaptor.forClass(ForecastRequest.class);
        verify(forecastClient).predict(captor.capture());
        ForecastRequest sent = captor.getValue();
        assertThat(sent.stationId()).isEqualTo(42);
        assertThat(sent.capacity()).isEqualTo(30);
        assertThat(sent.times()).hasSize(8).first().isEqualTo(LocalDateTime.parse("2026-10-28T18:00"));
        assertThat(sent.weather()).isNull();

        assertThat(result).isEqualTo(new StationForecast(42, "SMITHFIELD NORTH", 30, 3, false, PREDICTIONS));
    }

    @Test
    @DisplayName("9.1c Inference service unavailable: degraded = true, no predictions, but the real-time bike count is still included")
    void degradesWhenForecastUnavailable() {
        when(stations.getStation(42)).thenReturn(SMITHFIELD);
        when(forecastClient.predict(any())).thenReturn(Optional.empty());

        StationForecast result = serviceAt("2026-10-28T17:20:00Z").forecast(42, 8);

        assertThat(result.degraded()).isTrue();
        assertThat(result.predictions()).isEmpty();
        assertThat(result.currentBikes()).isEqualTo(3);
    }

    @Test
    @DisplayName("9.1 Station does not exist: StationNotFoundException, the inference service is not called")
    void stationNotFound() {
        when(stations.getStation(999)).thenThrow(new StationNotFoundException(999));

        assertThatThrownBy(() -> serviceAt("2026-10-28T17:20:00Z").forecast(999, 8))
                .isInstanceOf(StationNotFoundException.class);
        verify(forecastClient, never()).predict(any());
    }
}
