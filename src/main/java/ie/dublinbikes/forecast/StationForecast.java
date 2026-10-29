package ie.dublinbikes.forecast;

import java.util.List;

/**
 * Forecast for one station (domain object). Provided.
 *
 * degraded is true when the inference service is unavailable: predictions is empty, but currentBikes is still real-time data.
 */
public record StationForecast(int number, String name, int capacity, int currentBikes,
                              boolean degraded, List<HourlyPrediction> predictions) {
}
