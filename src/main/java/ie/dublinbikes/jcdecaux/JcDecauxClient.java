package ie.dublinbikes.jcdecaux;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import ie.dublinbikes.config.AppProperties;
import org.springframework.web.client.RestClientException;

/** Part 3.3 / 3.4：见练习册 Part 3 */
// TODO 3.3: 让 Spring 管理这个类
@Component
public class JcDecauxClient {

    private final RestClient restClient;
    private final AppProperties.Jcdecaux props;

    public JcDecauxClient(RestClient.Builder builder, AppProperties appProperties) {
        this.props = appProperties.jcdecaux();
        // TODO 3.3a: 用 builder 创建 RestClient，baseUrl 用 props.baseUrl()
        RestClient client = builder.baseUrl(props.baseUrl()).build();
        this.restClient = client;
    }

    public List<JcDecauxStationDto> fetchStations() {
        // TODO 3.3b: GET {baseUrl}/stations?contract={contract}&apiKey={apiKey}，把 JSON 数组转成 List<JcDecauxStationDto>
        // TODO 3.4:  对方返回 4xx/5xx，或者网络出错时，抛 JcDecauxException
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
