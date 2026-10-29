package ie.dublinbikes.forecast;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

@Configuration
public class ForecastResilienceConfig {

    @Bean
    public CircuitBreaker forecastCircuitBreaker(ForecastProperties properties) {
        ForecastProperties.Resilience r = properties.resilience();
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(r.windowSize())
                .minimumNumberOfCalls(r.windowSize())
                .failureRateThreshold(50)
                .waitDurationInOpenState(r.openFor())
                .permittedNumberOfCallsInHalfOpenState(1)
                .build();
        return CircuitBreaker.of("forecast", config);
    }

    @Bean
    public Retry forecastRetry(ForecastProperties properties) {
        ForecastProperties.Resilience r = properties.resilience();
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(r.maxAttempts())
                .waitDuration(r.retryWait())
                .retryOnException(e -> e instanceof ForecastUnavailableException f && f.isRetryable())
                .build();
        return Retry.of("forecast", config);
    }
}
