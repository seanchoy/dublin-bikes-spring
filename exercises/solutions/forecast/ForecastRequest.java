package ie.dublinbikes.forecast;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** POST /predict 的请求体。weather 为 null 时由 Python 服务自己推断天气。 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ForecastRequest(int stationId, int capacity, List<LocalDateTime> times, List<WeatherInput> weather) {
}
