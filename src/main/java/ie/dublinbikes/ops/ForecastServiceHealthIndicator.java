package ie.dublinbikes.ops;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import ie.dublinbikes.forecast.ForecastProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.web.client.RestClientException;

/** Part 10.1: see workbook Part 10 */
// TODO 10.1: let Spring manage this class (the Bean name decides that it shows up as forecastService in /actuator/health)
@Component
public class ForecastServiceHealthIndicator implements HealthIndicator {

    /** Response of the Python service's GET /health: {"status": "UP"}. Provided. */
    record ServiceStatus(String status) {
    }

    private final RestClient restClient;
    private final String baseUrl;
    private final CircuitBreaker circuitBreaker;

    public ForecastServiceHealthIndicator(RestClient.Builder builder, ForecastProperties properties,
                                          CircuitBreaker circuitBreaker) {
        this.baseUrl = properties.baseUrl();
        // TODO 10.1a: build the RestClient from builder, with baseUrl this.baseUrl
        this.restClient = builder.baseUrl(this.baseUrl).build();
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public Health health() {
        // TODO 10.1b: GET /health → up if status is "UP", otherwise down; on errors, down with an "error" detail;
        //             always add the "baseUrl" and "circuitBreaker" (circuit breaker state name) details

        try {
            ServiceStatus status = restClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(ServiceStatus.class);

            if("UP".equals(status.status)){
                return Health.up()
                        .withDetail("baseUrl", baseUrl)
                        .withDetail("circuitBreaker", circuitBreaker.getState().toString())
                        .build();
            }else{
                return Health.down()
                        .withDetail("baseUrl", baseUrl)
                        .withDetail("circuitBreaker", circuitBreaker.getState().toString())
                        .build();
            }
        } catch (RestClientException e) {

            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("baseUrl", baseUrl)
                    .withDetail("circuitBreaker", circuitBreaker.getState().toString())
                    .build();

        }

    }
}
