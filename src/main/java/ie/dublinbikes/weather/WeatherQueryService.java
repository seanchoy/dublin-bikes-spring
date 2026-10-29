package ie.dublinbikes.weather;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import ie.dublinbikes.weather.openweather.OpenWeatherClient;
import ie.dublinbikes.weather.persistence.CurrentWeatherEntity;
import ie.dublinbikes.weather.persistence.CurrentWeatherRepository;
import ie.dublinbikes.weather.persistence.ForecastWeatherEntity;
import ie.dublinbikes.weather.persistence.ForecastWeatherRepository;
import org.springframework.stereotype.Service;

/** Part 13.1: see workbook Part 13 */
// TODO 13.1: mark as a Service and run every method in a read-only transaction
@Service
public class WeatherQueryService {

    private final CurrentWeatherRepository currentRepository;
    private final ForecastWeatherRepository forecastRepository;
    private final OpenWeatherClient client;
    private final Clock clock;

    public WeatherQueryService(CurrentWeatherRepository currentRepository,
                               ForecastWeatherRepository forecastRepository,
                               OpenWeatherClient client,
                               Clock clock) {
        this.currentRepository = currentRepository;
        this.forecastRepository = forecastRepository;
        this.client = client;
        this.clock = clock;
    }

    public WeatherSnapshot current() {
        // TODO 13.1a: the latest row in the database; if the database is empty, call client.current()

        Optional<CurrentWeatherEntity> currentWeatherEntity = currentRepository.latest();
        if(currentWeatherEntity.isEmpty()){
            return client.current();
        }else{
            return currentWeatherEntity.get().toSnapshot();
        }

    }

    public List<WeatherSnapshot> forecast(int limit) {
        // TODO 13.1b: at most limit rows from "now" onwards in the database; if there are none, use client.forecast(),
        //             again keeping only slots from "now" onwards, at most limit
        Instant now = clock.instant();
        List<ForecastWeatherEntity> forecastWeatherEntities = forecastRepository.upcoming(now, limit);
        List<WeatherSnapshot> forecastWeatherSnapshots = forecastWeatherEntities.stream()
                .map(f -> f.toSnapshot())
                .toList();

        if(forecastWeatherSnapshots.isEmpty()){
            return client.forecast().stream()
                    .filter(f -> !f.time().isBefore(now))
                    .limit(limit)
                    .toList();
        }else{
            return forecastWeatherSnapshots;
        }

    }
}
