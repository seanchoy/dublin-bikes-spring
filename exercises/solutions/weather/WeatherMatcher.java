package ie.dublinbikes.weather;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import ie.dublinbikes.forecast.WeatherInput;

/** 把 3 小时一档的天气预报，对应到预测用的每一个整点。纯逻辑，不依赖 Spring。 */
public final class WeatherMatcher {

    private WeatherMatcher() {
    }

    public static List<WeatherInput> match(List<LocalDateTime> times, List<WeatherSnapshot> forecast, ZoneId zone) {
        if (forecast.isEmpty()) {
            return null;
        }
        return times.stream()
                .map(t -> toInput(nearest(t.atZone(zone).toInstant(), forecast)))
                .toList();
    }

    static WeatherSnapshot nearest(Instant target, List<WeatherSnapshot> forecast) {
        return forecast.stream()
                .min(Comparator.comparing((WeatherSnapshot w) -> Duration.between(w.time(), target).abs())
                        .thenComparing(WeatherSnapshot::time))
                .orElseThrow();
    }

    static WeatherInput toInput(WeatherSnapshot w) {
        boolean raining = w.rainMm() > 0.01 || w.rainProbability() >= 0.5;
        return new WeatherInput(w.temperature(), w.humidity(), w.pressure(), raining ? 1 : 0);
    }
}
