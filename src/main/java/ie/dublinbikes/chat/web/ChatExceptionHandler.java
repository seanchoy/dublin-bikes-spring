package ie.dublinbikes.chat.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import ie.dublinbikes.chat.ChatException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Part 17.3: see workbook Part 17 */
// TODO 17.3: REST exception handler that only applies to ChatController
@RestControllerAdvice(assignableTypes = ChatController.class)
public class ChatExceptionHandler {

    // TODO 17.3: handle ChatException:
    //   RATE_LIMITED → 429 with a Retry-After header and retryAfterSeconds in the ProblemDetail;
    //   NOT_CONFIGURED, UNAVAILABLE → 503 with title "Chat unavailable"
    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ProblemDetail> handleChat(ChatException e) {

        HttpStatus status = switch (e.getReason()){
            case RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;
            case NOT_CONFIGURED, UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
        };

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());

        if(e.getReason() == ChatException.Reason.RATE_LIMITED){
            problem.setProperty("retryAfterSeconds", e.getRetryAfterSeconds());
        }else{
            problem.setTitle("Chat unavailable");
        }

        return switch (e.getReason()){
            case RATE_LIMITED -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfterSeconds()))
                    .body(problem);
            case NOT_CONFIGURED, UNAVAILABLE -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(problem);
        };

    }

    // TODO 17.3: handle IllegalArgumentException → 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadMessages(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
