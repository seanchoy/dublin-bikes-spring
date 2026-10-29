package ie.dublinbikes.chat.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ie.dublinbikes.chat.ChatException;

@RestControllerAdvice(assignableTypes = ChatController.class)
public class ChatExceptionHandler {

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ProblemDetail> handleChat(ChatException e) {
        return switch (e.getReason()) {
            case RATE_LIMITED -> {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
                problem.setTitle("Too many requests");
                problem.setProperty("retryAfterSeconds", e.getRetryAfterSeconds());
                yield ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfterSeconds()))
                        .body(problem);
            }
            case NOT_CONFIGURED, UNAVAILABLE -> {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
                problem.setTitle("Chat unavailable");
                yield ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
            }
        };
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadMessages(IllegalArgumentException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Invalid chat request");
        return problem;
    }
}
