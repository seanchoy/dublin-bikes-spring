package ie.dublinbikes.weather;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import ie.dublinbikes.weather.ingest.WeatherIngestResult;
import ie.dublinbikes.weather.ingest.WeatherIngestService;
import ie.dublinbikes.weather.persistence.CurrentWeatherRepository;
import ie.dublinbikes.weather.persistence.ForecastWeatherRepository;
import jakarta.persistence.EntityManager;

/** Part 12.2: writing weather to the database. Current weather is insert-only; forecasts are upserted by forecast time (new forecasts overwrite old ones). */
@DataJpaTest
@Import({WeatherIngestService.class, Part12IngestServiceTest.FixedClock.class})
class Part12IngestServiceTest {

    static final Instant NOW = Instant.parse("2026-10-28T18:05:00Z");

    @TestConfiguration
    static class FixedClock {
        @Bean
        Clock clock() {
            return Clock.fixed(NOW, ZoneId.of("Europe/Dublin"));
        }
    }

    static WeatherSnapshot w(String time, double temp, double pop) {
        return new WeatherSnapshot(Instant.parse(time), temp, temp - 2, 85, 1012, 4.5, "clouds", 0.0, pop);
    }

    static final WeatherSnapshot CURRENT = w("2026-10-28T18:00:00Z", 9.4, 0.0);
    static final List<WeatherSnapshot> FORECAST = List.of(
            w("2026-10-28T21:00:00Z", 9.0, 0.6), w("2026-10-29T00:00:00Z", 8.1, 0.2), w("2026-10-29T03:00:00Z", 7.6, 0.0));

    @Autowired
    WeatherIngestService ingestService;

    @Autowired
    CurrentWeatherRepository currentRepository;

    @Autowired
    ForecastWeatherRepository forecastRepository;

    @Autowired
    EntityManager entityManager;

    void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Example: first write inserts 1 current-weather row and 3 forecast rows, with fetchedAt taken from clock")
    void firstIngest() {
        WeatherIngestResult result = ingestService.ingest(CURRENT, FORECAST);
        flushAndClear();

        assertThat(result).isEqualTo(new WeatherIngestResult(true, 3));
        assertThat(currentRepository.count()).isEqualTo(1);
        assertThat(forecastRepository.count()).isEqualTo(3);
        assertThat(forecastRepository.findById(Instant.parse("2026-10-28T21:00:00Z")).orElseThrow().getFetchedAt())
                .isEqualTo(NOW);
    }

    @Test
    @DisplayName("The same current weather again: not inserted twice (idempotent)")
    void currentIsIdempotent() {
        ingestService.ingest(CURRENT, List.of());
        flushAndClear();

        WeatherIngestResult again = ingestService.ingest(CURRENT, List.of());
        flushAndClear();

        assertThat(again.currentInserted()).isFalse();
        assertThat(currentRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("A new round of forecasts: the same forecast time overwrites the old value without adding a row (upsert)")
    void forecastIsUpserted() {
        ingestService.ingest(CURRENT, FORECAST);
        flushAndClear();

        ingestService.ingest(w("2026-10-28T18:10:00Z", 9.2, 0.0), List.of(w("2026-10-28T21:00:00Z", 6.5, 0.9)));
        flushAndClear();

        assertThat(forecastRepository.count()).isEqualTo(3);
        WeatherSnapshot updated = forecastRepository.findById(Instant.parse("2026-10-28T21:00:00Z")).orElseThrow().toSnapshot();
        assertThat(updated.temperature()).isEqualTo(6.5);
        assertThat(updated.rainProbability()).isEqualTo(0.9);
        assertThat(currentRepository.count()).isEqualTo(2);
    }
}
