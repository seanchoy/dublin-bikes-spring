package ie.dublinbikes.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

import ie.dublinbikes.station.StationNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Every exception thrown by a controller passes through here and is turned into RFC 9457 ProblemDetail JSON.
 *
 * Part 6.3: see workbook Part 6
 */
// TODO 6.3a: mark as a global REST exception handler that extends Spring's ResponseEntityExceptionHandler
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // TODO 6.3a: declare that this method handles StationNotFoundException
    @ExceptionHandler(StationNotFoundException.class)
    public ProblemDetail handleStationNotFound(StationNotFoundException e) {
        // TODO 6.3a: return 404 with title "Station not found" and the exception message as detail,
        //            plus an extension field stationNumber
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Station not found");
        problem.setProperty("stationNumber", e.getNumber());
        return problem;
    }
}
