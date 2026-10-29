package ie.dublinbikes.weather.web;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ie.dublinbikes.weather.WeatherQueryService;

@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {

    private final WeatherQueryService weatherService;

    public WeatherController(WeatherQueryService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current")
    public WeatherResponse current() {
        return WeatherResponse.from(weatherService.current());
    }

    @GetMapping("/forecast")
    public List<WeatherResponse> forecast(@RequestParam(defaultValue = "8") @Min(1) @Max(40) int limit) {
        return weatherService.forecast(limit).stream().map(WeatherResponse::from).toList();
    }
}
