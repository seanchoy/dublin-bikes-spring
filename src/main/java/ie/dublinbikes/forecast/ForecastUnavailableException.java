package ie.dublinbikes.forecast;

/**
 * Calling the Python inference service failed. Provided.
 *
 * status is the HTTP status the service returned; it is 0 for network errors (connection refused, timeout).
 */
public class ForecastUnavailableException extends RuntimeException {

    private final int status;

    /** The service returned 4xx / 5xx. */
    public ForecastUnavailableException(String message, int status) {
        super(message);
        this.status = status;
    }

    /** Network error: connection refused, timeout, connection reset. */
    public ForecastUnavailableException(String message, Throwable cause) {
        super(message, cause);
        this.status = 0;
    }

    public int getStatus() {
        return status;
    }

    /** Worth retrying? Network errors and 5xx may be temporary; 4xx means the request itself is wrong, so retrying will not help. */
    public boolean isRetryable() {
        return status == 0 || status >= 500;
    }
}
