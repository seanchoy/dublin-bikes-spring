package ie.dublinbikes.weather;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Enables binding for the weather configuration. Provided. */
@Configuration
@EnableConfigurationProperties(OpenWeatherProperties.class)
public class WeatherConfig {
}
