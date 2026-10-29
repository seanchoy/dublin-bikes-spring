package ie.dublinbikes.station.ingest;

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
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import ie.dublinbikes.config.AppConfig;
import ie.dublinbikes.config.AppProperties;
import ie.dublinbikes.jcdecaux.JcDecauxClient;
import ie.dublinbikes.jcdecaux.JcDecauxException;
import ie.dublinbikes.jcdecaux.JcDecauxStationDto;

/**
 * Part 5.2 / 5.3: the scheduled job.
 * A plain unit test without Spring: every dependency is a Mockito fake and the object under test is created with new.
 * This works precisely because we use constructor injection.
 */
class Part5ScrapeJobTest {

    final JcDecauxClient client = mock(JcDecauxClient.class);
    final StationIngestService ingestService = mock(StationIngestService.class);

    static AppProperties props(boolean enabled) {
        return new AppProperties(
                new AppProperties.Jcdecaux("key", "dublin", "https://example.test"),
                new AppProperties.Scraper(enabled, Duration.ofMinutes(5)));
    }

    @Test
    @DisplayName("5.2 When the switch is off, JCDecaux is not called at all")
    void disabledDoesNothing() {
        new StationScrapeJob(client, ingestService, props(false)).run();

        verifyNoInteractions(client, ingestService);
    }

    @Test
    @DisplayName("5.2 When the switch is on, it fetches data and passes it to StationIngestService")
    void enabledFetchesAndIngests() {
        List<JcDecauxStationDto> dtos = List.of(new JcDecauxStationDto(42, "dublin", "SMITHFIELD NORTH", "Smithfield",
                new JcDecauxStationDto.Position(53.35, -6.27), false, false, 30, 27, 3, "OPEN", 1761400800000L));
        when(client.fetchStations()).thenReturn(dtos);
        when(ingestService.ingest(anyList())).thenReturn(new IngestResult(1, 1));

        new StationScrapeJob(client, ingestService, props(true)).run();

        verify(ingestService).ingest(dtos);
    }

    @Test
    @DisplayName("5.2 When JCDecaux fails it only logs, without throwing or writing to the database")
    void failureIsSwallowed() {
        when(client.fetchStations()).thenThrow(new JcDecauxException("JCDecaux returned HTTP 503"));

        StationScrapeJob job = new StationScrapeJob(client, ingestService, props(true));

        assertThatCode(job::run).doesNotThrowAnyException();
        verify(ingestService, never()).ingest(any());
    }

    @Test
    @DisplayName("5.3 run() is annotated with @Scheduled and reads its interval from app.scraper.interval")
    void runIsScheduled() throws NoSuchMethodException {
        Scheduled scheduled = StationScrapeJob.class.getMethod("run").getAnnotation(Scheduled.class);

        assertThat(scheduled).as("run() should be annotated with @Scheduled").isNotNull();
        assertThat(scheduled.fixedDelayString()).contains("app.scraper.interval");
    }

    @Test
    @DisplayName("5.3 AppConfig enables scheduled tasks")
    void schedulingEnabled() {
        assertThat(AppConfig.class.isAnnotationPresent(EnableScheduling.class))
                .as("AppConfig should be annotated with @EnableScheduling")
                .isTrue();
    }
}
