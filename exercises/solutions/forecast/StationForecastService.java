package ie.dublinbikes.forecast;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import ie.dublinbikes.station.StationQueryService;
import ie.dublinbikes.station.domain.Station;

@Service
public class StationForecastService {

    private final StationQueryService stationQueryService;
    private final ResilientForecastClient forecastClient;
    private final Clock clock;

    public StationForecastService(StationQueryService stationQueryService,
                                  ResilientForecastClient forecastClient,
                                  Clock clock) {
        this.stationQueryService = stationQueryService;
        this.forecastClient = forecastClient;
        this.clock = clock;
    }

    public List<LocalDateTime> forecastTimes(int hours) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.truncatedTo(ChronoUnit.HOURS);
        if (now.isAfter(start)) {
            start = start.plusHours(1);
        }
        LocalDateTime first = start;
        return IntStream.range(0, hours)
                .mapToObj(first::plusHours)
                .toList();
    }

    public StationForecast forecast(int number, int hours) {
        Station station = stationQueryService.getStation(number);
        ForecastRequest request = new ForecastRequest(number, station.totalStands(), forecastTimes(hours), null);
        Optional<List<HourlyPrediction>> predictions = forecastClient.predict(request);
        return new StationForecast(number, station.name(), station.totalStands(), station.availableBikes(),
                predictions.isEmpty(), predictions.orElse(List.of()));
    }
}
