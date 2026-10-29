package ie.dublinbikes.weather.ingest;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ie.dublinbikes.weather.WeatherSnapshot;
import ie.dublinbikes.weather.persistence.CurrentWeatherEntity;
import ie.dublinbikes.weather.persistence.CurrentWeatherRepository;
import ie.dublinbikes.weather.persistence.ForecastWeatherEntity;
import ie.dublinbikes.weather.persistence.ForecastWeatherRepository;

@Service
public class WeatherIngestService {

    private final CurrentWeatherRepository currentRepository;
    private final ForecastWeatherRepository forecastRepository;
    private final Clock clock;

    public WeatherIngestService(CurrentWeatherRepository currentRepository,
                                ForecastWeatherRepository forecastRepository,
                                Clock clock) {
        this.currentRepository = currentRepository;
        this.forecastRepository = forecastRepository;
        this.clock = clock;
    }

    @Transactional
    public WeatherIngestResult ingest(WeatherSnapshot current, List<WeatherSnapshot> forecast) {
        boolean currentInserted = false;
        if (!currentRepository.existsById(current.time())) {
            currentRepository.save(CurrentWeatherEntity.from(current));
            currentInserted = true;
        }

        Instant now = Instant.now(clock);
        for (WeatherSnapshot w : forecast) {
            ForecastWeatherEntity entity = forecastRepository.findById(w.time())
                    .orElseGet(() -> new ForecastWeatherEntity(w.time()));
            entity.update(w, now);
            forecastRepository.save(entity);
        }
        return new WeatherIngestResult(currentInserted, forecast.size());
    }
}
