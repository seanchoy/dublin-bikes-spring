package ie.dublinbikes.weather.persistence;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrentWeatherRepository extends JpaRepository<CurrentWeatherEntity, Instant> {

    Optional<CurrentWeatherEntity> findFirstByOrderByObservedAtDesc();

    default Optional<CurrentWeatherEntity> latest() {
        return findFirstByOrderByObservedAtDesc();
    }
}
