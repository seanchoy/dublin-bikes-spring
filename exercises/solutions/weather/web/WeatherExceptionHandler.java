package ie.dublinbikes.weather.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ie.dublinbikes.weather.WeatherUnavailableException;

@RestControllerAdvice
public class WeatherExceptionHandler {

    @ExceptionHandler(WeatherUnavailableException.class)
    public ProblemDetail handleWeatherUnavailable(WeatherUnavailableException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        problem.setTitle("Weather unavailable");
        return problem;
    }
}
