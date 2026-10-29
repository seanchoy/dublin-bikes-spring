package ie.dublinbikes.weather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import ie.dublinbikes.config.AppProperties;
import ie.dublinbikes.weather.ingest.WeatherIngestResult;
import ie.dublinbikes.weather.ingest.WeatherIngestService;
import ie.dublinbikes.weather.ingest.WeatherScrapeJob;
import ie.dublinbikes.weather.openweather.OpenWeatherClient;

/** Part 12.3: the scheduled weather scrape (same pattern as StationScrapeJob in Part 5). */
class Part12ScrapeJobTest {

    final OpenWeatherClient client = mock(OpenWeatherClient.class);
    final WeatherIngestService ingestService = mock(WeatherIngestService.class);

    static AppProperties props(boolean enabled) {
        return new AppProperties(new AppProperties.Jcdecaux("key", "dublin", "https://example.test"),
                new AppProperties.Scraper(enabled, Duration.ofMinutes(5)));
    }

    static final WeatherSnapshot NOW =
            new WeatherSnapshot(Instant.parse("2026-10-28T18:00:00Z"), 9.4, 7.1, 87, 1012, 5.1, "rain", 0.4, 0.0);

    @Test
    @DisplayName("12.3 Switch off: does nothing")
    void disabled() {
        new WeatherScrapeJob(client, ingestService, props(false)).run();

        verifyNoInteractions(client, ingestService);
    }

    @Test
    @DisplayName("12.3 Switch on: fetches current weather and forecasts and passes them to WeatherIngestService together")
    void enabled() {
        List<WeatherSnapshot> forecast = List.of(NOW);
        when(client.current()).thenReturn(NOW);
        when(client.forecast()).thenReturn(forecast);
        when(ingestService.ingest(any(), anyList())).thenReturn(new WeatherIngestResult(true, 1));

        new WeatherScrapeJob(client, ingestService, props(true)).run();

        verify(ingestService).ingest(NOW, forecast);
    }

    @Test
    @DisplayName("12.3 OpenWeather fails: only logs, without throwing or writing to the database")
    void failureIsSwallowed() {
        when(client.current()).thenThrow(new WeatherUnavailableException("OpenWeather returned HTTP 401"));

        assertThatCode(new WeatherScrapeJob(client, ingestService, props(true))::run).doesNotThrowAnyException();
        verify(ingestService, never()).ingest(any(), anyList());
    }

    @Test
    @DisplayName("12.3 run() is annotated with @Scheduled and reads its interval from app.weather.interval")
    void scheduled() throws NoSuchMethodException {
        Scheduled scheduled = WeatherScrapeJob.class.getMethod("run").getAnnotation(Scheduled.class);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.fixedDelayString()).contains("app.weather.interval");
    }
}
