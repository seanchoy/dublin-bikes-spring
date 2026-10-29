package ie.dublinbikes.weather.web;

import java.time.Instant;

import ie.dublinbikes.weather.WeatherSnapshot;

/** JSON returned to the frontend by /api/v1/weather/*. Provided. */
public record WeatherResponse(Instant time, double temperature, double feelsLike, int humidity, int pressure,
                              double windSpeed, String description, double rainMm, double rainProbability) {

    public static WeatherResponse from(WeatherSnapshot w) {
        return new WeatherResponse(w.time(), w.temperature(), w.feelsLike(), w.humidity(), w.pressure(),
                w.windSpeed(), w.description(), w.rainMm(), w.rainProbability());
    }
}
