package ie.dublinbikes.forecast;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;

/**
 * Response body of POST /predict.
 *
 * Part 7.1: see workbook Part 7
 */
// TODO 7.1: deserialize fields as snake_case (station_id)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ForecastResponse(int stationId, List<HourlyPrediction> predictions) {
}
