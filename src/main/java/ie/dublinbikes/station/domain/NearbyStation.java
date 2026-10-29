package ie.dublinbikes.station.domain;

/** A station plus its distance (in metres) from the query point. */
public record NearbyStation(Station station, long distanceMeters) {
}
