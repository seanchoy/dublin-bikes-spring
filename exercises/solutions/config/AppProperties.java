package ie.dublinbikes.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** application.yml 里所有 app.* 配置，启动时由 Spring 自动填进来。 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(@DefaultValue Jcdecaux jcdecaux, @DefaultValue Scraper scraper) {

    public record Jcdecaux(
            String apiKey,
            @DefaultValue("dublin") String contract,
            @DefaultValue("https://api.jcdecaux.com/vls/v1") String baseUrl) {
    }

    public record Scraper(
            @DefaultValue("false") boolean enabled,
            @DefaultValue("5m") Duration interval) {
    }
}
