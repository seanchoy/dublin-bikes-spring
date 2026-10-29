package ie.dublinbikes.journey;

/**
 * Planning a journey failed. Provided.
 *
 * reason decides the status code returned to the frontend: address not found / no station available is 404, a Google error is 502.
 */
public class JourneyException extends RuntimeException {

    public enum Reason { ADDRESS_NOT_FOUND, NO_STATION_AVAILABLE, ROUTE_UNAVAILABLE }

    private final Reason reason;

    public JourneyException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
