package ie.dublinbikes.journey;

/** Travel mode; the value is the Google Directions mode parameter. Provided. */
public enum TravelMode {
    WALKING("walking"),
    BICYCLING("bicycling");

    private final String param;

    TravelMode(String param) {
        this.param = param;
    }

    public String param() {
        return param;
    }
}
