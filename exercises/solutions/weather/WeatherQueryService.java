package ie.dublinbikes.weather;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ie.dublinbikes.weather.openweather.OpenWeatherClient;
import ie.dublinbikes.weather.persistence.CurrentWeatherEntity;
import ie.dublinbikes.weather.persistence.CurrentWeatherRepository;
import ie.dublinbikes.weather.persistence.ForecastWeatherEntity;
import ie.dublinbikes.weather.persistence.ForecastWeatherRepository;

@Service
@Transactional(readOnly = true)
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
        return currentRepository.latest()
                .map(CurrentWeatherEntity::toSnapshot)
                .orElseGet(client::current);
    }

    public List<WeatherSnapshot> forecast(int limit) {
        Instant now = Instant.now(clock);
        List<WeatherSnapshot> stored = forecastRepository.upcoming(now, limit).stream()
                .map(ForecastWeatherEntity::toSnapshot)
                .toList();
        if (!stored.isEmpty()) {
            return stored;
        }
        return client.forecast().stream()
                .filter(w -> !w.time().isBefore(now))
                .limit(limit)
                .toList();
    }
}
