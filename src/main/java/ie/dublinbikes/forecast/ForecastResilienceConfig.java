package ie.dublinbikes.forecast;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Part 8.1: see workbook Part 8 */
// TODO 8.1: this is a configuration class
@Configuration
public class ForecastResilienceConfig {

    // TODO 8.1a: register as a Bean
    @Bean
    public CircuitBreaker forecastCircuitBreaker(ForecastProperties properties) {
        // TODO 8.1a: a circuit breaker named "forecast":
        //   look at the last windowSize calls, and only start judging after at least windowSize calls;
        //   open at a 50% failure rate, go half-open after openFor, and let only 1 trial request through when half-open
        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(properties.resilience().windowSize())
                .minimumNumberOfCalls(properties.resilience().windowSize())
                .failureRateThreshold(50)
                .waitDurationInOpenState(properties.resilience().openFor())
                .permittedNumberOfCallsInHalfOpenState(1)
                .build();
        CircuitBreaker breaker = CircuitBreaker.of("forecast", cbConfig);
        return breaker;
    }

    // TODO 8.1b: register as a Bean
    @Bean
    public Retry forecastRetry(ForecastProperties properties) {
        // TODO 8.1b: a retry named "forecast": at most maxAttempts attempts, retryWait between them
        // TODO 8.3:  only retry ForecastUnavailableException when isRetryable() is true
        RetryConfig rtConfig = RetryConfig.custom()
                .maxAttempts(properties.resilience().maxAttempts())
                .waitDuration(properties.resilience().retryWait())
                .retryOnException( e -> e instanceof ForecastUnavailableException && ((ForecastUnavailableException) e).isRetryable() )
                .build();
        Retry retry = Retry.of("forecast", rtConfig);
        return retry;
    }
}
