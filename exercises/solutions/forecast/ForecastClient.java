package ie.dublinbikes.forecast;

import java.util.List;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ForecastClient {

    private final RestClient restClient;

    public ForecastClient(RestClient.Builder builder, ForecastProperties properties) {
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
    }

    public List<HourlyPrediction> predict(ForecastRequest request) {
        try {
            ForecastResponse response = restClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        int status = res.getStatusCode().value();
                        throw new ForecastUnavailableException("Forecast service returned HTTP " + status, status);
                    })
                    .body(ForecastResponse.class);
            return response.predictions();
        } catch (RestClientException e) {
            throw new ForecastUnavailableException("Could not reach forecast service: " + e.getMessage(), e);
        }
    }
}
