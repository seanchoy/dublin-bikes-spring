package ie.dublinbikes.weather.ingest;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import ie.dublinbikes.weather.WeatherSnapshot;
import ie.dublinbikes.weather.persistence.CurrentWeatherEntity;
import ie.dublinbikes.weather.persistence.CurrentWeatherRepository;
import ie.dublinbikes.weather.persistence.ForecastWeatherEntity;
import ie.dublinbikes.weather.persistence.ForecastWeatherRepository;
import org.springframework.stereotype.Service;

/** Part 12.2: see workbook Part 12 */
// TODO 12.2: mark as a Service
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

    // TODO 12.2: run the whole method in one transaction
    public WeatherIngestResult ingest(WeatherSnapshot current, List<WeatherSnapshot> forecast) {
        // TODO 12.2a: current weather: insert only if observedAt is not in the database yet (CurrentWeatherEntity.from)
        // TODO 12.2b: forecast: upsert each slot by forecastTime (new ForecastWeatherEntity(time) if not found),
        //             write it with update(snapshot, now), taking "now" from clock

        Instant now = current.time();
        boolean currentInserted = false;
        if(!currentRepository.existsById(now)){
            currentRepository.save(CurrentWeatherEntity.from(current));
            currentInserted = true;
        }

        int forecastSaved = 0;
        for(WeatherSnapshot ws: forecast){
            ForecastWeatherEntity forecastWeatherEntity = forecastRepository.findById(ws.time())
                    .orElseGet(() -> new ForecastWeatherEntity(ws.time()));
            forecastWeatherEntity.update(ws, clock.instant());
            forecastRepository.save(forecastWeatherEntity);
            forecastSaved++;
        }
        return new WeatherIngestResult(currentInserted, forecastSaved);
    }
}
