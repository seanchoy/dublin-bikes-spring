package ie.dublinbikes.forecast;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * One hour of weather sent to the Python service.
 *
 * Part 7.1: see workbook Part 7
 */

// TODO 7.1: the Python JSON fields are snake_case (avg_temp, is_raining, ...).
//           Use one annotation to serialize every field of this record as snake_case, instead of @JsonProperty on each one
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record WeatherInput(double avgTemp, double avgHumidity, double avgPressure, int isRaining) {
}
