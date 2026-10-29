package ie.dublinbikes.forecast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

/**
 * Part 8: retry, circuit breaker and graceful degradation.
 * No Spring here: the circuit breaker and retry come straight from the @Bean methods in ForecastResilienceConfig,
 * and Mockito provides a fake ForecastClient that succeeds or fails as each test needs.
 */
class Part8ResilienceTest {

    static final ForecastProperties PROPS = new ForecastProperties("http://forecast.test",
            new ForecastProperties.Resilience(2, Duration.ofMillis(1), 5, Duration.ofMinutes(1)));

    static final ForecastRequest REQUEST =
            new ForecastRequest(42, 30, List.of(LocalDateTime.parse("2026-10-28T18:00:00")), null);

    static final List<HourlyPrediction> PREDICTIONS = List.of(
            new HourlyPrediction(LocalDateTime.parse("2026-10-28T18:00:00"), 12.4, 17.6, 8.0, 83.5, 1014.5, 0));

    final ForecastResilienceConfig config = new ForecastResilienceConfig();
    ForecastClient client;
    CircuitBreaker circuitBreaker;
    Retry retry;
    ResilientForecastClient resilient;

    @BeforeEach
    void setUp() {
        client = mock(ForecastClient.class);
        circuitBreaker = config.forecastCircuitBreaker(PROPS);
        retry = config.forecastRetry(PROPS);
        resilient = new ResilientForecastClient(client, circuitBreaker, retry);
    }

    static ForecastUnavailableException serverError() {
        return new ForecastUnavailableException("Forecast service returned HTTP 503", 503);
    }

    @Test
    @DisplayName("8.1 The circuit breaker is configured from ForecastProperties")
    void circuitBreakerConfig() {
        assertThat(circuitBreaker.getName()).isEqualTo("forecast");
        var c = circuitBreaker.getCircuitBreakerConfig();
        assertThat(c.getSlidingWindowSize()).isEqualTo(5);
        assertThat(c.getMinimumNumberOfCalls()).isEqualTo(5);
        assertThat(c.getFailureRateThreshold()).isEqualTo(50f);
        assertThat(c.getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(1);
    }

    @Test
    @DisplayName("8.1 Retry: at most maxAttempts attempts")
    void retryConfig() {
        assertThat(retry.getName()).isEqualTo("forecast");
        assertThat(retry.getRetryConfig().getMaxAttempts()).isEqualTo(2);
    }

    @Test
    @DisplayName("8.2 Succeeds first time: returns the result after one call")
    void success() {
        when(client.predict(REQUEST)).thenReturn(PREDICTIONS);

        assertThat(resilient.predict(REQUEST)).contains(PREDICTIONS);
        verify(client, times(1)).predict(REQUEST);
    }

    @Test
    @DisplayName("8.2 503 first, then success: the retry gets the result")
    void retriesOnceThenSucceeds() {
        when(client.predict(REQUEST)).thenThrow(serverError()).thenReturn(PREDICTIONS);

        assertThat(resilient.predict(REQUEST)).contains(PREDICTIONS);
        verify(client, times(2)).predict(REQUEST);
    }

    @Test
    @DisplayName("8.2 Keeps failing: gives up after 2 attempts and returns Optional.empty() (degraded) without throwing")
    void givesUpAndDegrades() {
        when(client.predict(any())).thenThrow(serverError());

        assertThat(resilient.predict(REQUEST)).isEmpty();
        verify(client, times(2)).predict(REQUEST);
    }

    @Test
    @DisplayName("8.3 A request error such as 422 is not retried: only one call")
    void doesNotRetryClientErrors() {
        when(client.predict(any())).thenThrow(new ForecastUnavailableException("HTTP 422", 422));

        assertThat(resilient.predict(REQUEST)).isEmpty();
        verify(client, times(1)).predict(REQUEST);
    }

    @Test
    @DisplayName("8.2 After 5 failures in a row the circuit opens; the next call degrades without calling the Python service")
    void opensAfterRepeatedFailures() {
        when(client.predict(any())).thenThrow(serverError());

        for (int i = 0; i < 5; i++) {
            assertThat(resilient.predict(REQUEST)).isEmpty();
        }
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        verify(client, times(10)).predict(REQUEST);        // 5 calls × 2 attempts each

        assertThat(resilient.predict(REQUEST)).isEmpty();
        verify(client, times(10)).predict(REQUEST);        // circuit open: no new calls
    }
}
