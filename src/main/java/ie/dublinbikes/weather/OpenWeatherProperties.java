package ie.dublinbikes.weather;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Configuration under app.openweather.* and app.weather.*. Provided. */
@ConfigurationProperties(prefix = "app")
public record OpenWeatherProperties(@DefaultValue OpenWeather openweather, @DefaultValue Weather weather) {

    public record OpenWeather(
            String apiKey,
            @DefaultValue("https://api.openweathermap.org/data/2.5") String baseUrl,
            @DefaultValue("53.3498") double lat,
            @DefaultValue("-6.2603") double lon) {
    }

    /** @param interval how often to scrape the weather */
    public record Weather(@DefaultValue("10m") Duration interval) {
    }
}
