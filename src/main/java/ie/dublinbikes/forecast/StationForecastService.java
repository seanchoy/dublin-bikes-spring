package ie.dublinbikes.forecast;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import ie.dublinbikes.station.StationQueryService;
import ie.dublinbikes.station.domain.Station;
import ie.dublinbikes.weather.WeatherMatcher;
import ie.dublinbikes.weather.WeatherQueryService;
import org.springframework.cglib.core.Local;
import org.springframework.data.util.Optionals;
import org.springframework.stereotype.Service;

/** Part 9.1: see workbook Part 9 */
// TODO 9.1: mark as a Service
@Service
public class StationForecastService {

    private final StationQueryService stationQueryService;
    private final ResilientForecastClient forecastClient;
    private final WeatherQueryService weatherQueryService;
    private final Clock clock;

    public StationForecastService(StationQueryService stationQueryService,
                                  ResilientForecastClient forecastClient,
                                  Clock clock,
                                  WeatherQueryService weatherQueryService) {
        this.stationQueryService = stationQueryService;
        this.forecastClient = forecastClient;
        this.weatherQueryService = weatherQueryService;
        this.clock = clock;
    }

    public List<LocalDateTime> forecastTimes(int hours) {
        // TODO 9.1a: get "now" from clock and start at the next full hour (or now, if it is exactly on the hour),
        //            one per hour, hours in total

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime hour = now.truncatedTo(ChronoUnit.HOURS);
        boolean later = now.isAfter(hour);
        final LocalDateTime hourNow;
        if(later){
            hourNow = hour.plusHours(1);
        }else{
            hourNow = hour;
        }


        List<LocalDateTime> times = IntStream.range(0, hours)
                .mapToObj(i -> hourNow.plusHours(i))
                .toList();

        return times;

    }

    public StationForecast forecast(int number, int hours) {
        // TODO 9.1b: load the station → build a ForecastRequest (weather null) → call forecastClient
        // TODO 9.1c: when the inference service is unavailable, return degraded = true and an empty predictions list

        Station station = stationQueryService.getStation(number);
        List<LocalDateTime> times = forecastTimes(hours);
        List<WeatherInput> weather = WeatherMatcher.match(times, weatherQueryService.forecast(16), clock.getZone());
        ForecastRequest forecastRequest = new ForecastRequest(number, station.totalStands(), times, weather);

        List<HourlyPrediction> forecastPredictions = forecastClient.predict(forecastRequest).orElse(List.of());
        boolean degraded = forecastPredictions.isEmpty();

        return new StationForecast(number,
                station.name(),
                station.totalStands(), station.availableBikes(),
                degraded, forecastPredictions);



    }
}
