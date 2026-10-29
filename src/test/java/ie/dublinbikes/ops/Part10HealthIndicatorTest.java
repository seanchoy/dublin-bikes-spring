package ie.dublinbikes.ops;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import ie.dublinbikes.forecast.ForecastPropertiesConfig;
import ie.dublinbikes.forecast.ForecastResilienceConfig;

/** Part 10.1: health check for the inference service. */
@RestClientTest(ForecastServiceHealthIndicator.class)
@Import({ForecastPropertiesConfig.class, ForecastResilienceConfig.class})
@TestPropertySource(properties = "app.forecast.base-url=http://forecast.test")
class Part10HealthIndicatorTest {

    static final String URL = "http://forecast.test/health";

    @Autowired
    ForecastServiceHealthIndicator indicator;

    @Autowired
    MockRestServiceServer server;

    @Test
    @DisplayName("10.1 Python service returns {\"status\":\"UP\"}: UP, with baseUrl and the circuit breaker state")
    void up() {
        server.expect(requestTo(URL)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"status\":\"UP\"}", MediaType.APPLICATION_JSON));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("baseUrl", "http://forecast.test")
                .containsEntry("circuitBreaker", "CLOSED");
    }

    @Test
    @DisplayName("10.1 Python service reports DOWN (e.g. the model has not finished loading): DOWN")
    void reportsDown() {
        server.expect(requestTo(URL)).andRespond(withSuccess("{\"status\":\"DOWN\"}", MediaType.APPLICATION_JSON));

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    @DisplayName("10.1 Returns 503: DOWN, without throwing")
    void serverError() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error").containsEntry("circuitBreaker", "CLOSED");
    }

    @Test
    @DisplayName("10.1 Cannot connect: DOWN, with an error in the details")
    void unreachable() {
        server.expect(requestTo(URL)).andRespond(withException(new IOException("connection refused")));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error");
    }
}
