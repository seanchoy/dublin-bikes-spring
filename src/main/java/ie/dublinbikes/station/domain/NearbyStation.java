package ie.dublinbikes.station.domain;

/** 一个站点加上它到查询点的距离（米）。 */
public record NearbyStation(Station station, long distanceMeters) {
}
