package ie.dublinbikes.journey;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Configuration under app.google.*. Provided. */
@ConfigurationProperties(prefix = "app.google")
public record GoogleMapsProperties(
        String serverKey,
        String browserKey,
        @DefaultValue("https://maps.googleapis.com/maps/api") String baseUrl) {
}
