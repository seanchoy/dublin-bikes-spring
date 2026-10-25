package ie.dublinbikes.jcdecaux;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import ie.dublinbikes.config.AppProperties;

@Component
public class JcDecauxClient {

    private final RestClient restClient;
    private final AppProperties.Jcdecaux props;

    public JcDecauxClient(RestClient.Builder builder, AppProperties appProperties) {
        this.props = appProperties.jcdecaux();
        this.restClient = builder.baseUrl(props.baseUrl()).build();
    }

    public List<JcDecauxStationDto> fetchStations() {
        try {
            return restClient.get()
                    .uri(uri -> uri.path("/stations")
                            .queryParam("contract", props.contract())
                            .queryParam("apiKey", props.apiKey())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new JcDecauxException("JCDecaux returned HTTP " + response.getStatusCode().value());
                    })
                    .body(new ParameterizedTypeReference<List<JcDecauxStationDto>>() {
                    });
        } catch (RestClientException e) {
            throw new JcDecauxException("Could not reach JCDecaux: " + e.getMessage(), e);
        }
    }
}
