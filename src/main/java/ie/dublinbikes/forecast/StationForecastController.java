package ie.dublinbikes.forecast;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.web.bind.annotation.*;

/** Part 9.2 / 9.3: see workbook Part 9 */
// TODO 9.2: REST controller with the same base path as StationController, /api/v1/stations
@RestController
@RequestMapping("/api/v1/stations")
@Tag(name = "Forecast")
public class StationForecastController {

    private final StationForecastService forecastService;


    public StationForecastController(StationForecastService forecastService) {
        this.forecastService = forecastService;
    }

    // TODO 9.2: GET /api/v1/stations/{number}/forecast?hours=.., hours defaults to 8
    // TODO 9.3: hours must be between 1 and 48
    @GetMapping("/{number}/forecast")
    @Operation(summary = "Forecast bikes for the next hours")
    public StationForecastResponse forecast(
            @PathVariable int number,
            @Parameter(description = "between 1-48") @RequestParam(defaultValue = "8") @Min(1) @Max(48) int hours) {

        return StationForecastResponse.from(forecastService.forecast(number, hours));

    }
}
