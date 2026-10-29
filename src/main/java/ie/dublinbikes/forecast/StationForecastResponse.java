package ie.dublinbikes.forecast;

import java.util.List;

/** JSON returned to the frontend by GET /api/v1/stations/{number}/forecast. Provided. */
public record StationForecastResponse(int number, String name, int capacity, int currentBikes,
                                      boolean degraded, List<ForecastPointResponse> predictions) {

    public static StationForecastResponse from(StationForecast f) {
        return new StationForecastResponse(f.number(), f.name(), f.capacity(), f.currentBikes(), f.degraded(),
                f.predictions().stream().map(ForecastPointResponse::from).toList());
    }
}
