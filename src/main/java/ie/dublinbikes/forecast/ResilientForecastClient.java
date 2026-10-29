package ie.dublinbikes.forecast;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import org.springframework.stereotype.Service;

/** Part 8.2: see workbook Part 8 */
// TODO 8.2: mark as a Service
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
        // TODO 8.2: wrap client.predict(request) in the circuit breaker (outer) and retry (inner):
        //   success → Optional.of(result)
        //   circuit breaker open (CallNotPermittedException) or still failing in the end (ForecastUnavailableException)
        //   → log a warning and return Optional.empty(); do not rethrow

        Supplier<List<HourlyPrediction>> call = () -> client.predict(request);
        Supplier<List<HourlyPrediction>> protectedCall = CircuitBreaker.decorateSupplier(circuitBreaker,
                Retry.decorateSupplier(retry, call));

        try{
            Optional<List<HourlyPrediction>> predictions = Optional.of(protectedCall.get());
            return predictions;
        }catch(CallNotPermittedException e){
            log.warn("Forecast Circuit breaker is open, skipping call");
            return Optional.empty();
        }catch(ForecastUnavailableException e){
            log.warn("Forecast failed after retries: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
