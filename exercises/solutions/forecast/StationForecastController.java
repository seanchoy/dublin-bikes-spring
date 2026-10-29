package ie.dublinbikes.forecast;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stations")
public class StationForecastController {

    private final StationForecastService forecastService;

    public StationForecastController(StationForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GetMapping("/{number}/forecast")
    public StationForecastResponse forecast(
            @PathVariable int number,
            @RequestParam(defaultValue = "8") @Min(1) @Max(48) int hours) {
        return StationForecastResponse.from(forecastService.forecast(number, hours));
    }
}
