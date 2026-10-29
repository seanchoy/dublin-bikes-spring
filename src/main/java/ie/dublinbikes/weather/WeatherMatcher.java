package ie.dublinbikes.weather;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import ie.dublinbikes.forecast.WeatherInput;

/**
 * Matches the 3-hourly weather forecast to each hourly time point used for prediction. Pure logic, no Spring.
 *
 * Part 13.3: see workbook Part 13
 */
public final class WeatherMatcher {

    private WeatherMatcher() {
    }

    public static List<WeatherInput> match(List<LocalDateTime> times, List<WeatherSnapshot> forecast, ZoneId zone) {
        // TODO 13.3: for each hour (local time; convert it to an Instant with zone first), find the closest forecast slot and convert it to a WeatherInput;
        //            on a tie take the earlier slot; return null if forecast is empty

        if(forecast.isEmpty()){
            return null;
        }

        List<WeatherInput> results = new ArrayList<>();
        for(LocalDateTime time: times){

            Instant timeUTC = time.atZone(zone).toInstant();

            WeatherSnapshot best = forecast.stream()
                    .min(Comparator.comparing((WeatherSnapshot ws) -> Duration.between(ws.time(), timeUTC).abs())
                                    .thenComparing(ws -> ws.time()))
                    .orElseThrow();


            results.add(new WeatherInput(
                    best.temperature(),
                    best.humidity(),
                    best.pressure(),
                    (best.rainProbability() >= 0.5 || best.rainMm() > 0.01) ? 1 : 0));
        }

        return results;
    }
}
