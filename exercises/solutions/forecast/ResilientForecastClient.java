package ie.dublinbikes.forecast;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

@Service
public class ResilientForecastClient {

    private static final Logger log = LoggerFactory.getLogger(ResilientForecastClient.class);

    private final ForecastClient client;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public ResilientForecastClient(ForecastClient client, CircuitBreaker circuitBreaker, Retry retry) {
        this.client = client;
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
    }

    public Optional<List<HourlyPrediction>> predict(ForecastRequest request) {
        Supplier<List<HourlyPrediction>> call = () -> client.predict(request);
        Supplier<List<HourlyPrediction>> protectedCall =
                CircuitBreaker.decorateSupplier(circuitBreaker, Retry.decorateSupplier(retry, call));
        try {
            return Optional.of(protectedCall.get());
        } catch (CallNotPermittedException e) {
            log.warn("Forecast skipped: circuit breaker is {}", circuitBreaker.getState());
            return Optional.empty();
        } catch (ForecastUnavailableException e) {
            log.warn("Forecast failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
