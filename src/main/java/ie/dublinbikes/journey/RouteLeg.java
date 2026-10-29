package ie.dublinbikes.journey;

/** One leg of a route (walking or cycling). Provided. */
public record RouteLeg(TravelMode mode, long distanceMeters, long durationSeconds,
                       String distanceText, String durationText, String polyline) {
}
