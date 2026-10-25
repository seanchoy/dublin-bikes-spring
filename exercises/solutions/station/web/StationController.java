package ie.dublinbikes.station.web;

import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ie.dublinbikes.station.StationQueryService;

@RestController
@RequestMapping("/api/v1/stations")
public class StationController {

    private final StationQueryService queryService;

    public StationController(StationQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public List<StationResponse> listStations() {
        return queryService.listStations().stream()
                .map(StationResponse::from)
                .toList();
    }

    @GetMapping("/{number}")
    public StationResponse getStation(@PathVariable int number) {
        return StationResponse.from(queryService.getStation(number));
    }

    @GetMapping("/nearest")
    public List<NearbyStationResponse> nearest(
            @RequestParam @DecimalMin("-90") @DecimalMax("90") double lat,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double lng,
            @RequestParam(defaultValue = "3") @Min(1) @Max(20) int limit,
            @RequestParam(defaultValue = "false") boolean requireBikes) {
        return queryService.findNearest(lat, lng, limit, requireBikes).stream()
                .map(NearbyStationResponse::from)
                .toList();
    }
}
