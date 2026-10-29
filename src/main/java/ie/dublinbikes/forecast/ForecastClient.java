package ie.dublinbikes.forecast;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/** Part 7.2: see workbook Part 7 */
// TODO 7.2: let Spring manage this class
@Component
public class ForecastClient {

    private final RestClient restClient;

    public ForecastClient(RestClient.Builder builder, ForecastProperties properties) {
        // TODO 7.2a: build the RestClient from builder, with baseUrl properties.baseUrl()
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
    }

    public List<HourlyPrediction> predict(ForecastRequest request) {
        // TODO 7.2b: POST {baseUrl}/predict with request as the JSON body, read a ForecastResponse and return its predictions
        // TODO 7.2c: on 4xx / 5xx throw ForecastUnavailableException(message, status);
        //            on network errors throw ForecastUnavailableException(message, cause)
        try{
            ForecastResponse forecastResponse = restClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request) // serialized as JSON
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        int status = res.getStatusCode().value();
                        throw new ForecastUnavailableException("Forecast service returned " + status, status);
                    })
                    .body(ForecastResponse.class);
            return forecastResponse.predictions();
        } catch (ResourceAccessException e){
            throw new ForecastUnavailableException("Forecast Service Unreachable", e);
        }
    }
}
