package ie.dublinbikes.weather.persistence;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForecastWeatherRepository extends JpaRepository<ForecastWeatherEntity, Instant> {

    List<ForecastWeatherEntity> findByForecastTimeGreaterThanEqualOrderByForecastTimeAsc(Instant from, Limit limit);

    default List<ForecastWeatherEntity> upcoming(Instant from, int limit) {
        return findByForecastTimeGreaterThanEqualOrderByForecastTimeAsc(from, Limit.of(limit));
    }
}
