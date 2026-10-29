package ie.dublinbikes.weather.persistence;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

/** Part 12.1: see workbook Part 12 */
public interface ForecastWeatherRepository extends JpaRepository<ForecastWeatherEntity, Instant> {

    // TODO 12.1b: declare a derived query: forecastTime >= from, ordered by forecastTime ascending, at most limit rows
    List<ForecastWeatherEntity> findByForecastTimeGreaterThanEqualOrderByForecastTimeAsc(Instant from, Limit Limit);

    default List<ForecastWeatherEntity> upcoming(Instant from, int limit) {
        // TODO 12.1b: call the method you declared
        return findByForecastTimeGreaterThanEqualOrderByForecastTimeAsc(from, Limit.of(limit));
    }
}
