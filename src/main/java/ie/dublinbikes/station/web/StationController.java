package ie.dublinbikes.station.web;

import java.util.List;

import ie.dublinbikes.station.StationQueryService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

/** Part 6.2: see workbook Part 6 */
// TODO 6.2: mark as a REST controller with every endpoint under /api/v1/stations
@RestController
@RequestMapping("/api/v1/stations")
public class StationController {

    private final StationQueryService queryService;

    public StationController(StationQueryService queryService) {
        this.queryService = queryService;
    }

    // TODO 6.2a: GET /api/v1/stations
    @GetMapping
    public List<StationResponse> listStations() {
        return queryService.listStations()
                .stream()
                .map(a -> StationResponse.from(a))
                .toList();
    }

    // TODO 6.2b: GET /api/v1/stations/{number}, with number taken from the URL path
    @GetMapping("/{number}")
    public StationResponse getStation(@PathVariable int number) {
        return StationResponse.from(queryService.getStation(number));
    }

    // TODO 6.2c: GET /api/v1/stations/nearest?lat=..&lng=..&limit=..&requireBikes=..
    //   - lat and lng are required; limit defaults to 3; requireBikes defaults to false
    @GetMapping("/nearest")
    // TODO 6.3b: validation: lat between -90 and 90, lng between -180 and 180, limit between 1 and 20
    public List<NearbyStationResponse> nearest(
            @RequestParam @DecimalMin("-90") @DecimalMax("90") double lat,
            @RequestParam  @DecimalMin("-180")@DecimalMax("180") double lng,
            @RequestParam(defaultValue = "3") @Min(1) @Max(20) int limit,
            @RequestParam(defaultValue = "false") boolean requireBikes) {

        return queryService.findNearest(lat, lng, limit, requireBikes).stream()
                .map(a -> NearbyStationResponse.from(a))
                .toList();
    }
}
