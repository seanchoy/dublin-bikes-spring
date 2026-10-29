package ie.dublinbikes.journey;

/** A latitude / longitude pair. Provided. */
public record LatLng(double lat, double lng) {

    /** The format Google expects: "53.3498,-6.2603" */
    public String toParam() {
        return lat + "," + lng;
    }
}
