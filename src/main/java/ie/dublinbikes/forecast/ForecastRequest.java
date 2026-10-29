package ie.dublinbikes.forecast;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request body for POST /predict. When weather is null, the Python service infers the weather itself.
 *
 * Part 7.1 / 7.3: see workbook Part 7
 */
// TODO 7.1: serialize fields as snake_case (station_id, ...)
// TODO 7.3: when weather is null, leave the weather field out of the JSON
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ForecastRequest(int stationId, int capacity, List<LocalDateTime> times, List<WeatherInput> weather) {
}
