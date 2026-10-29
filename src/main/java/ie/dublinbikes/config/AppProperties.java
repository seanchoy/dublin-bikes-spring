package ie.dublinbikes.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * All app.* settings from application.yml, filled in by Spring at startup.
 *
 * Part 2.1 / 2.2: see workbook Part 2
 */
// TODO 2.1: tell Spring this record maps the settings that start with "app"
@ConfigurationProperties(prefix = "app")
public record AppProperties(@DefaultValue Jcdecaux jcdecaux, @DefaultValue Scraper scraper) {

    // TODO 2.2: add defaults for the fields below: contract "dublin",
    //           baseUrl "https://api.jcdecaux.com/vls/v1", enabled false, interval 5 minutes.
    //           Also make sure jcdecaux() and scraper() are not null when nothing is configured.

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
