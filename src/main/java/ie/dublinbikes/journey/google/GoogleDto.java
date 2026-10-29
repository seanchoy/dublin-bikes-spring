package ie.dublinbikes.journey.google;

import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** JSON returned by Google Geocoding / Directions (only the fields we use). Provided. */
public final class GoogleDto {

    private GoogleDto() {
    }

    public record Location(double lat, double lng) {
    }

    public record Geometry(Location location) {
    }

    public record GeocodeResult(Geometry geometry) {
    }

    /** GET /geocode/json */
    public record GeocodeResponse(String status, List<GeocodeResult> results) {
    }

    public record TextValue(String text, long value) {
    }

    public record Leg(TextValue distance, TextValue duration) {
    }

    public record Polyline(String points) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Route(List<Leg> legs, Polyline overviewPolyline) {
    }

    /** GET /directions/json */
    public record DirectionsResponse(String status, List<Route> routes) {
    }
}
