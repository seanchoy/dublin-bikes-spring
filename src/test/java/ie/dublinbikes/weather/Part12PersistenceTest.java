package ie.dublinbikes.weather;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import ie.dublinbikes.weather.persistence.CurrentWeatherEntity;
import ie.dublinbikes.weather.persistence.CurrentWeatherRepository;
import ie.dublinbikes.weather.persistence.ForecastWeatherEntity;
import ie.dublinbikes.weather.persistence.ForecastWeatherRepository;
import jakarta.persistence.EntityManager;

/** Part 12.1: weather entities and repositories (tables defined in V2__create_weather_tables.sql). */
@DataJpaTest
class Part12PersistenceTest {

    static WeatherSnapshot at(String time, double temp) {
        return new WeatherSnapshot(Instant.parse(time), temp, temp - 2, 85, 1012, 4.5, "light rain", 0.3, 0.6);
    }

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
    @DisplayName("12.1 Current weather saved and read back is unchanged (rainProbability is always 0)")
    void currentRoundTrip() {
        currentRepository.save(CurrentWeatherEntity.from(at("2026-10-28T18:00:00Z", 9.4)));
        flushAndClear();

        WeatherSnapshot loaded = currentRepository.findById(Instant.parse("2026-10-28T18:00:00Z"))
                .orElseThrow().toSnapshot();
        assertThat(loaded).isEqualTo(new WeatherSnapshot(Instant.parse("2026-10-28T18:00:00Z"),
                9.4, 7.4, 85, 1012, 4.5, "light rain", 0.3, 0.0));
    }

    @Test
    @DisplayName("12.1 latest(): the newest row, or Optional.empty() when the table is empty")
    void latestCurrent() {
        assertThat(currentRepository.latest()).isEmpty();

        currentRepository.saveAll(List.of(
                CurrentWeatherEntity.from(at("2026-10-28T18:00:00Z", 9.4)),
                CurrentWeatherEntity.from(at("2026-10-28T18:20:00Z", 9.1)),
                CurrentWeatherEntity.from(at("2026-10-28T18:10:00Z", 9.3))));
        flushAndClear();

        assertThat(currentRepository.latest()).get()
                .extracting(CurrentWeatherEntity::getObservedAt)
                .isEqualTo(Instant.parse("2026-10-28T18:20:00Z"));
    }

    @Test
    @DisplayName("12.1 A forecast saved and read back, including fetchedAt")
    void forecastRoundTrip() {
        ForecastWeatherEntity e = new ForecastWeatherEntity(Instant.parse("2026-10-28T21:00:00Z"));
        e.update(at("2026-10-28T21:00:00Z", 8.0), Instant.parse("2026-10-28T18:00:00Z"));
        forecastRepository.save(e);
        flushAndClear();

        ForecastWeatherEntity loaded = forecastRepository.findById(Instant.parse("2026-10-28T21:00:00Z")).orElseThrow();
        assertThat(loaded.toSnapshot()).isEqualTo(at("2026-10-28T21:00:00Z", 8.0));
        assertThat(loaded.getFetchedAt()).isEqualTo(Instant.parse("2026-10-28T18:00:00Z"));
    }

    @Test
    @DisplayName("12.1 upcoming(from, limit): only from onwards, ordered by time ascending, at most limit rows")
    void upcomingForecast() {
        for (String t : List.of("2026-10-28T15:00:00Z", "2026-10-28T18:00:00Z", "2026-10-29T00:00:00Z",
                "2026-10-28T21:00:00Z", "2026-10-29T03:00:00Z")) {
            ForecastWeatherEntity e = new ForecastWeatherEntity(Instant.parse(t));
            e.update(at(t, 8.0), Instant.parse("2026-10-28T12:00:00Z"));
            forecastRepository.save(e);
        }
        flushAndClear();

        assertThat(forecastRepository.upcoming(Instant.parse("2026-10-28T18:00:00Z"), 3))
                .extracting(ForecastWeatherEntity::getForecastTime)
                .containsExactly(Instant.parse("2026-10-28T18:00:00Z"), Instant.parse("2026-10-28T21:00:00Z"),
                        Instant.parse("2026-10-29T00:00:00Z"));
    }
}
