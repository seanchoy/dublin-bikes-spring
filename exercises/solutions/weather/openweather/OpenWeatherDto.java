package ie.dublinbikes.weather.openweather;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ie.dublinbikes.weather.WeatherSnapshot;

/** OpenWeather 返回的 JSON 结构。 */
public final class OpenWeatherDto {

    private OpenWeatherDto() {
    }

    public record Main(double temp, @JsonProperty("feels_like") double feelsLike, int humidity, int pressure) {
    }

    public record Wind(double speed) {
    }

    public record Condition(int id, String description) {
    }

    public record Rain(@JsonProperty("1h") Double oneHour, @JsonProperty("3h") Double threeHours) {
    }

    /** GET /weather */
    public record Current(long dt, Main main, Wind wind, List<Condition> weather, Rain rain) {

        public WeatherSnapshot toSnapshot() {
            double rainMm = rain != null && rain.oneHour() != null ? rain.oneHour() : 0.0;
            return new WeatherSnapshot(Instant.ofEpochSecond(dt), main.temp(), main.feelsLike(), main.humidity(),
                    main.pressure(), wind.speed(), describe(weather), rainMm, 0.0);
        }
    }

    /** GET /forecast 的一项（每 3 小时一项） */
    public record Item(long dt, Main main, Wind wind, List<Condition> weather, Rain rain, double pop) {

        public WeatherSnapshot toSnapshot() {
            double rainMm = rain != null && rain.threeHours() != null ? rain.threeHours() : 0.0;
            return new WeatherSnapshot(Instant.ofEpochSecond(dt), main.temp(), main.feelsLike(), main.humidity(),
                    main.pressure(), wind.speed(), describe(weather), rainMm, pop);
        }
    }

    /** GET /forecast */
    public record Forecast(List<Item> list) {
    }

    static String describe(List<Condition> conditions) {
        return conditions == null || conditions.isEmpty() ? "" : conditions.get(0).description();
    }
}
