package ie.dublinbikes.forecast;

import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** POST /predict 的响应体。 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ForecastResponse(int stationId, List<HourlyPrediction> predictions) {
}
