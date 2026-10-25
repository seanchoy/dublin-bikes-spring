package ie.dublinbikes.jcdecaux;

/** 调用 JCDecaux 失败时统一抛这个异常，调用方不用关心底层是 HTTP 错误还是网络错误。 */
public class JcDecauxException extends RuntimeException {

    public JcDecauxException(String message) {
        super(message);
    }

    public JcDecauxException(String message, Throwable cause) {
        super(message, cause);
    }
}
