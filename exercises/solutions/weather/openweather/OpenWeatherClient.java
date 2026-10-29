package ie.dublinbikes.weather.openweather;

import java.util.List;
import java.util.function.Function;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

import ie.dublinbikes.weather.OpenWeatherProperties;
import ie.dublinbikes.weather.WeatherSnapshot;
import ie.dublinbikes.weather.WeatherUnavailableException;

@Component
public class OpenWeatherClient {

    private final RestClient restClient;
    private final OpenWeatherProperties.OpenWeather props;

    public OpenWeatherClient(RestClient.Builder builder, OpenWeatherProperties properties) {
        this.props = properties.openweather();
        this.restClient = builder.baseUrl(props.baseUrl()).build();
    }

    @Cacheable("currentWeather")
    public WeatherSnapshot current() {
        return get("/weather", OpenWeatherDto.Current.class).toSnapshot();
    }

    @Cacheable("weatherForecast")
    public List<WeatherSnapshot> forecast() {
        return get("/forecast", OpenWeatherDto.Forecast.class).list().stream()
                .map(OpenWeatherDto.Item::toSnapshot)
                .toList();
    }

    private <T> T get(String path, Class<T> type) {
        Function<UriBuilder, java.net.URI> uri = b -> b.path(path)
                .queryParam("lat", props.lat())
                .queryParam("lon", props.lon())
                .queryParam("units", "metric")
                .queryParam("appid", props.apiKey())
                .build();
        try {
            return restClient.get().uri(uri).retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new WeatherUnavailableException("OpenWeather returned HTTP " + res.getStatusCode().value());
                    })
                    .body(type);
        } catch (RestClientException e) {
            throw new WeatherUnavailableException("Could not reach OpenWeather: " + e.getMessage(), e);
        }
    }
}
