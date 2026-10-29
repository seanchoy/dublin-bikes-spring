package ie.dublinbikes.ops;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import ie.dublinbikes.forecast.ForecastProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;

@Component
public class ForecastServiceHealthIndicator implements HealthIndicator {

    /** Python 服务 GET /health 的响应：{"status": "UP"}。 */
    record ServiceStatus(String status) {
    }

    private final RestClient restClient;
    private final String baseUrl;
    private final CircuitBreaker circuitBreaker;

    public ForecastServiceHealthIndicator(RestClient.Builder builder, ForecastProperties properties,
                                          CircuitBreaker circuitBreaker) {
        this.baseUrl = properties.baseUrl();
        this.restClient = builder.baseUrl(baseUrl).build();
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public Health health() {
        Health.Builder builder;
        try {
            ServiceStatus status = restClient.get().uri("/health").retrieve().body(ServiceStatus.class);
            builder = status != null && "UP".equals(status.status()) ? Health.up() : Health.down();
        } catch (RestClientException e) {
            builder = Health.down().withDetail("error", e.getMessage());
        }
        return builder
                .withDetail("baseUrl", baseUrl)
                .withDetail("circuitBreaker", circuitBreaker.getState().name())
                .build();
    }
}
