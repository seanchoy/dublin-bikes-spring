package ie.dublinbikes.weather;

import java.time.Instant;

/**
 * Weather at one moment (domain object), used for both current weather and forecasts. Provided.
 *
 * @param rainMm          rainfall in mm: the past hour for current weather, the 3-hour slot for forecasts
 * @param rainProbability probability of rain, 0 to 1 (forecasts only; 0 for current weather)
 */
public record WeatherSnapshot(Instant time, double temperature, double feelsLike, int humidity, int pressure,
                              double windSpeed, String description, double rainMm, double rainProbability) {
}
