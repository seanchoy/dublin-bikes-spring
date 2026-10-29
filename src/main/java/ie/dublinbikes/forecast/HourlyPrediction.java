package ie.dublinbikes.forecast;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;

/**
 * One hour of prediction returned by the Python service.
 *
 * Part 7.1: see workbook Part 7
 */
// TODO 7.1: deserialize fields as snake_case (avg_temp, is_raining, ...)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record HourlyPrediction(LocalDateTime time, double bikes, double docks,
                               double avgTemp, double avgHumidity, double avgPressure, int isRaining) {
}
