package ie.dublinbikes.config;

import java.time.Duration;

/**
 * application.yml 里所有 app.* 配置，启动时由 Spring 自动填进来。
 *
 * Part 2.1 / 2.2：见练习册 Part 2
 */
// TODO 2.1: 告诉 Spring 这个 record 对应配置文件里以 "app" 开头的配置
public record AppProperties(Jcdecaux jcdecaux, Scraper scraper) {

    // TODO 2.2: 给下面的字段加默认值：contract 默认 "dublin"，
    //           baseUrl 默认 "https://api.jcdecaux.com/vls/v1"，enabled 默认 false，interval 默认 5 分钟。
    //           还要保证什么都没配置时，jcdecaux() 和 scraper() 本身也不是 null。

    public record Jcdecaux(
            String apiKey,
            String contract,
            String baseUrl) {
    }

    public record Scraper(
            boolean enabled,
            Duration interval) {
    }
}
