package ie.dublinbikes.config;

import java.time.Clock;

/**
 * 应用级别的配置类。
 *
 * Part 2.1 / 2.3：见练习册 Part 2
 * Part 5.3：见练习册 Part 5
 */
// TODO 2.1: 把这个类标记成配置类，并启用 AppProperties 的配置绑定
// TODO 5.3: 开启定时任务功能
public class AppConfig {

    // TODO 2.3: 把这个方法的返回值注册成一个 Bean
    public Clock clock() {
        // TODO 2.3: 返回一个使用 Europe/Dublin 时区的系统时钟
        throw new UnsupportedOperationException("TODO 2.3");
    }
}
