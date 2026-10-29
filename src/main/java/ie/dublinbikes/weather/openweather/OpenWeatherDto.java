package ie.dublinbikes.weather.openweather;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import ie.dublinbikes.weather.WeatherSnapshot;

/**
 * JSON structures returned by OpenWeather.
 *
 * Part 11.1: see workbook Part 11
 */
public final class OpenWeatherDto {

    private OpenWeatherDto() {
    }

    // TODO 11.1a: called feels_like in the JSON
    public record Main(double temp,
                       @JsonProperty("feels_like") double feelsLike,
                       int humidity,
                       int pressure) {
    }

    public record Wind(double speed) {
    }

    public record Condition(int id, String description) {
    }

    // TODO 11.1a: called "1h" and "3h" in the JSON (Java names cannot start with a digit)
    public record Rain(
            @JsonProperty("1h") Double oneHour,
            @JsonProperty("3h") Double threeHours) {
    }

    /** GET /weather */
    public record Current(long dt, Main main, Wind wind, List<Condition> weather, Rain rain) {

        public WeatherSnapshot toSnapshot() {
            // TODO 11.1b: dt is a timestamp in seconds; rainfall is the 1-hour value of rain (0 if rain or the value is null);
            //             description is describe(weather); current weather has no rain probability, so pass 0
            Instant dtInstant = Instant.ofEpochSecond(dt);

            return new WeatherSnapshot(dtInstant,
                    main.temp(),
                    main.feelsLike(),
                    main.humidity(),
                    main.pressure(),
                    wind.speed(),
                    describe(weather),
                    Optional.ofNullable(rain)
                            .map(rain -> rain.oneHour())
                            .orElse(0.0),
                    0
                    );

        }
    }


    /** One item of GET /forecast (one every 3 hours) */
    public record Item(long dt, Main main, Wind wind, List<Condition> weather, Rain rain, double pop) {

        public WeatherSnapshot toSnapshot() {
            // TODO 11.1b: same as Current, but rainfall is the 3-hour value and rain probability is pop
            return new WeatherSnapshot(
                    Instant.ofEpochSecond(dt),
                    main.temp(),
                    main.feelsLike(),
                    main.humidity(),
                    main.pressure(),
                    wind.speed(),
                    describe(weather),
                    Optional.ofNullable(rain)
                            .map(rain -> rain.threeHours())
                            .orElse(0.0),
                    pop
            );


        }
    }

    /** GET /forecast */
    public record Forecast(List<Item> list) {
    }

    /** The first weather description, or an empty string if there is none. Provided. */
    static String describe(List<Condition> conditions) {
        return conditions == null || conditions.isEmpty() ? "" : conditions.get(0).description();
    }
}
