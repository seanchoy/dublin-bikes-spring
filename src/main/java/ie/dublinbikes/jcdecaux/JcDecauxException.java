package ie.dublinbikes.jcdecaux;

/** Thrown whenever calling JCDecaux fails, so callers do not need to care whether it was an HTTP or a network error. */
public class JcDecauxException extends RuntimeException {

    public JcDecauxException(String message) {
        super(message);
    }

    public JcDecauxException(String message, Throwable cause) {
        super(message, cause);
    }
}
