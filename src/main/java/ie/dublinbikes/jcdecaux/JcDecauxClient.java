package ie.dublinbikes.jcdecaux;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import ie.dublinbikes.config.AppProperties;
import org.springframework.web.client.RestClientException;

/** Part 3.3 / 3.4: see workbook Part 3 */
// TODO 3.3: let Spring manage this class
@Component
public class JcDecauxClient {

    private final RestClient restClient;
    private final AppProperties.Jcdecaux props;

    public JcDecauxClient(RestClient.Builder builder, AppProperties appProperties) {
        this.props = appProperties.jcdecaux();
        // TODO 3.3a: build the RestClient from builder, with baseUrl props.baseUrl()
        RestClient client = builder.baseUrl(props.baseUrl()).build();
        this.restClient = client;
    }

    public List<JcDecauxStationDto> fetchStations() {
        // TODO 3.3b: GET {baseUrl}/stations?contract={contract}&apiKey={apiKey}, convert the JSON array to List<JcDecauxStationDto>
        // TODO 3.4:  throw JcDecauxException on 4xx/5xx or network errors
        try{
            List<JcDecauxStationDto> stationDto = restClient.get()
                    .uri(uri -> uri.path("/stations")
                            .queryParam("contract", props.contract())
                            .queryParam("apiKey", props.apiKey())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new JcDecauxException("API returned HTTP " + response.getStatusCode().value());
                    })
                    .body(new ParameterizedTypeReference<List<JcDecauxStationDto>>(){});
            return stationDto;
        } catch (RestClientException e) {
            throw new JcDecauxException("Could not reach JCDecaux: " + e.getMessage(), e);
        }
    };
}
