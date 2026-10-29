package ie.dublinbikes.journey.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ie.dublinbikes.journey.JourneyException;

@RestControllerAdvice
public class JourneyExceptionHandler {

    @ExceptionHandler(JourneyException.class)
    public ProblemDetail handleJourney(JourneyException e) {
        HttpStatus status = switch (e.getReason()) {
            case ADDRESS_NOT_FOUND, NO_STATION_AVAILABLE -> HttpStatus.NOT_FOUND;
            case ROUTE_UNAVAILABLE -> HttpStatus.BAD_GATEWAY;
        };
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        problem.setTitle("Journey not available");
        problem.setProperty("reason", e.getReason().name());
        return problem;
    }
}
