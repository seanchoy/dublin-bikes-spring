package ie.dublinbikes.station.web;

import java.util.List;

import ie.dublinbikes.station.StationQueryService;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;

/** Part 6.2：见练习册 Part 6 */
// TODO 6.2: 标记成 REST Controller，所有接口都在 /api/v1/stations 下面
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

    // TODO 6.2b: GET /api/v1/stations/{number}，number 从 URL 路径里取
    @GetMapping("/{number}")
    public StationResponse getStation(@PathVariable int number) {
        return StationResponse.from(queryService.getStation(number));
    }

    // TODO 6.2c: GET /api/v1/stations/nearest?lat=..&lng=..&limit=..&requireBikes=..
    //   - lat、lng 必填；limit 默认 3；requireBikes 默认 false
    @GetMapping("/nearest")
    // TODO 6.3b: 参数校验：lat 在 -90~90，lng 在 -180~180，limit 在 1~20
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
