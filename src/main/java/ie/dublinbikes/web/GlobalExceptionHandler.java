package ie.dublinbikes.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

import ie.dublinbikes.station.StationNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * 所有 Controller 抛出的异常都会经过这里，统一转成 RFC 9457 的 ProblemDetail JSON。
 *
 * Part 6.3：见练习册 Part 6
 */
// TODO 6.3a: 标记成全局的 REST 异常处理器，并继承 Spring 自带的 ResponseEntityExceptionHandler
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // TODO 6.3a: 声明这个方法处理 StationNotFoundException
    @ExceptionHandler(StationNotFoundException.class)
    public ProblemDetail handleStationNotFound(StationNotFoundException e) {
        // TODO 6.3a: 返回 404，title 是 "Station not found"，detail 是异常消息，
        //            再加一个扩展字段 stationNumber
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Station not found");
        problem.setProperty("stationNumber", e.getNumber());
        return problem;
    }
}
