package ie.dublinbikes.station.web;

import java.util.List;

import ie.dublinbikes.station.StationQueryService;

/** Part 6.2：见练习册 Part 6 */
// TODO 6.2: 标记成 REST Controller，所有接口都在 /api/v1/stations 下面
public class StationController {

    private final StationQueryService queryService;

    public StationController(StationQueryService queryService) {
        this.queryService = queryService;
    }

    // TODO 6.2a: GET /api/v1/stations
    public List<StationResponse> listStations() {
        throw new UnsupportedOperationException("TODO 6.2a");
    }

    // TODO 6.2b: GET /api/v1/stations/{number}，number 从 URL 路径里取
    public StationResponse getStation(int number) {
        throw new UnsupportedOperationException("TODO 6.2b");
    }

    // TODO 6.2c: GET /api/v1/stations/nearest?lat=..&lng=..&limit=..&requireBikes=..
    //   - lat、lng 必填；limit 默认 3；requireBikes 默认 false
    // TODO 6.3b: 参数校验：lat 在 -90~90，lng 在 -180~180，limit 在 1~20
    public List<NearbyStationResponse> nearest(double lat, double lng, int limit, boolean requireBikes) {
        throw new UnsupportedOperationException("TODO 6.2c");
    }
}
