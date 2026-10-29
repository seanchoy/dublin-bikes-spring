package ie.dublinbikes.forecast;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Enables binding for ForecastProperties (same pattern as Part 2). Provided. */
@Configuration
@EnableConfigurationProperties(ForecastProperties.class)
public class ForecastPropertiesConfig {
}
