package ie.dublinbikes.journey;

import java.util.List;

/** One leg of a route (walking or cycling), with its turn-by-turn steps. Provided. */
public record RouteLeg(TravelMode mode, long distanceMeters, long durationSeconds,
                       String distanceText, String durationText, String polyline, List<RouteStep> steps) {

    public RouteLeg {
        steps = steps == null ? List.of() : List.copyOf(steps);
    }

    /** A leg without turn-by-turn steps. */
    public RouteLeg(TravelMode mode, long distanceMeters, long durationSeconds,
                    String distanceText, String durationText, String polyline) {
        this(mode, distanceMeters, durationSeconds, distanceText, durationText, polyline, List.of());
    }
}
