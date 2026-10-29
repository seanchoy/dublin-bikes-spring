package ie.dublinbikes.journey;

/**
 * The departure time is in the past, or beyond what the forecast covers (48 hours).
 */
public class InvalidDepartureException extends RuntimeException {

    public InvalidDepartureException(String message) {
        super(message);
    }
}
