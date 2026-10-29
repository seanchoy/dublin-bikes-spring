package ie.dublinbikes.journey.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import ie.dublinbikes.journey.JourneyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Part 15.3: see workbook Part 15 */
// TODO 15.3: global REST exception handler (do not extend ResponseEntityExceptionHandler)
@RestControllerAdvice
public class JourneyExceptionHandler {

    // TODO 15.3: handle JourneyException: ADDRESS_NOT_FOUND / NO_STATION_AVAILABLE → 404, ROUTE_UNAVAILABLE → 502;
    //            title "Journey not available", plus an extension field reason (the enum name)
    @ExceptionHandler(JourneyException.class)
    public ProblemDetail handleJourney(JourneyException e) {

        HttpStatus status = switch (e.getReason()){
            case ADDRESS_NOT_FOUND, NO_STATION_AVAILABLE -> HttpStatus.NOT_FOUND;
            case ROUTE_UNAVAILABLE -> HttpStatus.BAD_GATEWAY;
        };

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        problem.setTitle("Journey not available");
        problem.setProperty("reason", e.getReason().name());

        return problem;
    }
}
