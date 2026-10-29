package ie.dublinbikes.weather.persistence;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** Part 12.1: see workbook Part 12 */
public interface CurrentWeatherRepository extends JpaRepository<CurrentWeatherEntity, Instant> {

    // TODO 12.1a: declare a derived query: order by observedAt descending, first result only
    Optional<CurrentWeatherEntity> findFirstByOrderByObservedAtDesc();


    default Optional<CurrentWeatherEntity> latest() {
        // TODO 12.1a: call the method you declared
        return findFirstByOrderByObservedAtDesc();
    }
}
