package ie.dublinbikes.forecast;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Forecast", description = "ML predictions of station availability")
@RestController
@RequestMapping("/api/v1/stations")
public class StationForecastController {

    private final StationForecastService forecastService;

    public StationForecastController(StationForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @Operation(summary = "Forecast bikes for the next hours",
            description = "Hourly predictions starting at the next full hour. degraded = true when the ML service is unavailable.")
    @GetMapping("/{number}/forecast")
    public StationForecastResponse forecast(
            @PathVariable int number,
            @Parameter(description = "How many hours to forecast (1-48)")
            @RequestParam(defaultValue = "8") @Min(1) @Max(48) int hours) {
        return StationForecastResponse.from(forecastService.forecast(number, hours));
    }
}
