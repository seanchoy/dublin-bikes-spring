package ie.dublinbikes.forecast;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Configuration under app.forecast.* in application.yml. Provided. */
@ConfigurationProperties(prefix = "app.forecast")
public record ForecastProperties(
        @DefaultValue("http://localhost:8001") String baseUrl,
        @DefaultValue Resilience resilience) {

    /**
     * @param maxAttempts  how many attempts per call, including the first
     * @param retryWait    how long to wait between attempts
     * @param windowSize   how many recent calls the circuit breaker looks at
     * @param openFor      how long the circuit breaker stays open before letting one trial request through
     */
    public record Resilience(
            @DefaultValue("2") int maxAttempts,
            @DefaultValue("200ms") Duration retryWait,
            @DefaultValue("5") int windowSize,
            @DefaultValue("30s") Duration openFor) {
    }
}
