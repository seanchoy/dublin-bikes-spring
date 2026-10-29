package ie.dublinbikes.chat;

/**
 * Chat failed. Provided.
 *
 * NOT_CONFIGURED: GROQ_API_KEY is not set; RATE_LIMITED: Groq rate limit (retry after retryAfterSeconds); UNAVAILABLE: any other error.
 */
public class ChatException extends RuntimeException {

    public enum Reason { NOT_CONFIGURED, RATE_LIMITED, UNAVAILABLE }

    private final Reason reason;
    private final int retryAfterSeconds;

    public ChatException(Reason reason, String message) {
        this(reason, message, 0);
    }

    public ChatException(Reason reason, String message, int retryAfterSeconds) {
        super(message);
        this.reason = reason;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public Reason getReason() {
        return reason;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
