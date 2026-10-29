package ie.dublinbikes.forecast;

import java.time.LocalDateTime;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** Python 服务返回的某一个小时的预测。 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record HourlyPrediction(LocalDateTime time, double bikes, double docks,
                               double avgTemp, double avgHumidity, double avgPressure, int isRaining) {
}
