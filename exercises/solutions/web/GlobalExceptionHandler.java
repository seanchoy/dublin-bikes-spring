package ie.dublinbikes.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import ie.dublinbikes.station.StationNotFoundException;

/**
 * 所有 Controller 抛出的异常都会经过这里，统一转成 RFC 9457 的 ProblemDetail JSON。
 * 继承 ResponseEntityExceptionHandler 后，参数缺失、参数校验失败等 Spring 自带的异常也会变成 400 ProblemDetail。
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(StationNotFoundException.class)
    public ProblemDetail handleStationNotFound(StationNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Station not found");
        problem.setProperty("stationNumber", e.getNumber());
        return problem;
    }
}
