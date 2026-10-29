package ie.dublinbikes.forecast;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** 发给 Python 服务的一小时天气。JSON 字段是 snake_case：avg_temp、is_raining…… */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record WeatherInput(double avgTemp, double avgHumidity, double avgPressure, int isRaining) {
}
