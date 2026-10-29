package ie.dublinbikes.weather;

/** No weather available: OpenWeather failed and there is nothing in the database either. Provided. */
public class WeatherUnavailableException extends RuntimeException {

    public WeatherUnavailableException(String message) {
        super(message);
    }

    public WeatherUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
