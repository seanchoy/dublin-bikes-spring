package ie.dublinbikes.weather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ie.dublinbikes.weather.openweather.OpenWeatherClient;
import ie.dublinbikes.weather.persistence.CurrentWeatherEntity;
import ie.dublinbikes.weather.persistence.CurrentWeatherRepository;
import ie.dublinbikes.weather.persistence.ForecastWeatherEntity;
import ie.dublinbikes.weather.persistence.ForecastWeatherRepository;

/** Part 13.1: database first, OpenWeather as the fallback. */
class Part13QueryServiceTest {

    static final Instant NOW = Instant.parse("2026-10-28T18:05:00Z");

    final CurrentWeatherRepository currentRepository = mock(CurrentWeatherRepository.class);
    final ForecastWeatherRepository forecastRepository = mock(ForecastWeatherRepository.class);
    final OpenWeatherClient client = mock(OpenWeatherClient.class);
    final WeatherQueryService service = new WeatherQueryService(currentRepository, forecastRepository, client,
            Clock.fixed(NOW, ZoneId.of("Europe/Dublin")));

    static WeatherSnapshot w(String time, double temp) {
        return new WeatherSnapshot(Instant.parse(time), temp, temp - 2, 85, 1012, 4.5, "clouds", 0.0, 0.0);
    }

    static ForecastWeatherEntity stored(String time, double temp) {
        ForecastWeatherEntity e = new ForecastWeatherEntity(Instant.parse(time));
        e.update(w(time, temp), NOW);
        return e;
    }

    @Test
    @DisplayName("13.1a current: uses the database when it has data, without calling OpenWeather")
    void currentFromDatabase() {
        when(currentRepository.latest()).thenReturn(Optional.of(CurrentWeatherEntity.from(w("2026-10-28T18:00:00Z", 9.4))));

        assertThat(service.current().temperature()).isEqualTo(9.4);
        verify(client, never()).current();
    }

    @Test
    @DisplayName("13.1a current: calls OpenWeather when the database is empty")
    void currentFallsBackToApi() {
        when(currentRepository.latest()).thenReturn(Optional.empty());
        when(client.current()).thenReturn(w("2026-10-28T18:04:00Z", 9.9));

        assertThat(service.current().temperature()).isEqualTo(9.9);
    }

    @Test
    @DisplayName("13.1a Neither source has data: WeatherUnavailableException propagates (the controller turns it into 503)")
    void currentUnavailable() {
        when(currentRepository.latest()).thenReturn(Optional.empty());
        when(client.current()).thenThrow(new WeatherUnavailableException("OpenWeather returned HTTP 503"));

        assertThatThrownBy(service::current).isInstanceOf(WeatherUnavailableException.class);
    }

    @Test
    @DisplayName("13.1b forecast: from the database, from \"now\" onwards; the Repository gets the clock's time and limit")
    void forecastFromDatabase() {
        when(forecastRepository.upcoming(NOW, 2)).thenReturn(List.of(stored("2026-10-28T21:00:00Z", 9.0),
                stored("2026-10-29T00:00:00Z", 8.1)));

        assertThat(service.forecast(2)).extracting(WeatherSnapshot::temperature).containsExactly(9.0, 8.1);
        verify(client, never()).forecast();
    }

    @Test
    @DisplayName("13.1b forecast: no future forecasts in the database, so it uses OpenWeather's, again from \"now\" onwards and at most limit")
    void forecastFallsBackToApi() {
        when(forecastRepository.upcoming(any(), anyInt())).thenReturn(List.of());
        when(client.forecast()).thenReturn(List.of(w("2026-10-28T15:00:00Z", 10.0), w("2026-10-28T21:00:00Z", 9.0),
                w("2026-10-29T00:00:00Z", 8.1), w("2026-10-29T03:00:00Z", 7.6)));

        assertThat(service.forecast(2)).extracting(WeatherSnapshot::temperature).containsExactly(9.0, 8.1);
    }
}
