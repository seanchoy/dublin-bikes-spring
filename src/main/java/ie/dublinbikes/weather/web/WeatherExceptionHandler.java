package ie.dublinbikes.weather.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import ie.dublinbikes.weather.WeatherUnavailableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Part 13.2b: see workbook Part 13 */
// TODO 13.2b: global REST exception handler (note: do not extend ResponseEntityExceptionHandler)
@RestControllerAdvice
public class WeatherExceptionHandler {

    // TODO 13.2b: handle WeatherUnavailableException
    @ExceptionHandler(WeatherUnavailableException.class)
    public ProblemDetail handleWeatherUnavailable(WeatherUnavailableException e) {
        // TODO 13.2b: 503 with title "Weather unavailable" and the exception message as detail
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        problem.setTitle("Weather unavailable");
        return problem;
    }
}
