package ie.dublinbikes.weather.web;

import java.util.List;

import ie.dublinbikes.weather.WeatherQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.web.bind.annotation.*;

/** Part 13.2: see workbook Part 13 */
// TODO 13.2: REST controller with base path /api/v1/weather
@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {

    private final WeatherQueryService weatherService;

    public WeatherController(WeatherQueryService weatherService) {
        this.weatherService = weatherService;
    }

    // TODO 13.2: GET /api/v1/weather/current
    @GetMapping("/current")
    public WeatherResponse current() {

        return WeatherResponse.from(weatherService.current());


    }

    // TODO 13.2: GET /api/v1/weather/forecast?limit=.., limit defaults to 8, range 1 to 40
    @GetMapping("/forecast")
    public List<WeatherResponse> forecast(
            @RequestParam(defaultValue = "8") @Min(1) @Max(40) int limit) {

        return weatherService.forecast(limit)
                .stream()
                .map(WeatherResponse::from)
                .toList();

    }
}
