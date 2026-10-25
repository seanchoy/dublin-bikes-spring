package ie.dublinbikes.web;

import org.springframework.http.ProblemDetail;

import ie.dublinbikes.station.StationNotFoundException;

/**
 * 所有 Controller 抛出的异常都会经过这里，统一转成 RFC 9457 的 ProblemDetail JSON。
 *
 * Part 6.3：见练习册 Part 6
 */
// TODO 6.3a: 标记成全局的 REST 异常处理器，并继承 Spring 自带的 ResponseEntityExceptionHandler
public class GlobalExceptionHandler {

    // TODO 6.3a: 声明这个方法处理 StationNotFoundException
    public ProblemDetail handleStationNotFound(StationNotFoundException e) {
        // TODO 6.3a: 返回 404，title 是 "Station not found"，detail 是异常消息，
        //            再加一个扩展字段 stationNumber
        throw new UnsupportedOperationException("TODO 6.3a");
    }
}
