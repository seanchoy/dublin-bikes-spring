package ie.dublinbikes.weather.openweather;

import java.util.List;

import ie.dublinbikes.weather.WeatherUnavailableException;
import org.aspectj.apache.bcel.classfile.Module;
import org.hibernate.annotations.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import ie.dublinbikes.weather.OpenWeatherProperties;
import ie.dublinbikes.weather.WeatherSnapshot;
import org.springframework.web.client.RestClientException;

/** Part 11.2 / 11.3: see workbook Part 11 */
// TODO 11.2: let Spring manage this class
@Component
public class OpenWeatherClient {

    private final RestClient restClient;
    private final OpenWeatherProperties.OpenWeather props;

    public OpenWeatherClient(RestClient.Builder builder, OpenWeatherProperties properties) {
        this.props = properties.openweather();
        // TODO 11.2a: build the RestClient from builder, with baseUrl props.baseUrl()
        this.restClient = builder.baseUrl(this.props.baseUrl()).build();
    }

    // TODO 11.3: cache the result in a cache named "currentWeather"
    @Cacheable("currentWeather")
    public WeatherSnapshot current() {
        // TODO 11.2b: GET {baseUrl}/weather?lat=..&lon=..&units=metric&appid=.., read an OpenWeatherDto.Current and call toSnapshot()
        //             throw WeatherUnavailableException on errors
        try{
            OpenWeatherDto.Current currentWeather = restClient.get()
                    .uri(uri -> uri.path("/weather")
                            .queryParam("lat", props.lat())
                            .queryParam("lon", props.lon())
                            .queryParam("units", "metric")
                            .queryParam("appid", props.apiKey())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                            int statusCode = res.getStatusCode().value();
                            throw new WeatherUnavailableException("OpenWeather returned HTTP " + statusCode);})
                    .body(OpenWeatherDto.Current.class);

            WeatherSnapshot currentWeatherSnapshot = currentWeather.toSnapshot();
            return currentWeatherSnapshot;

        } catch (RestClientException e){
            throw new WeatherUnavailableException("Can not reach OpenWeather " + e.getMessage(), e);
        }

    }
    // TODO 11.3: cache the result in a cache named "weatherForecast"
    @Cacheable("weatherForecast")
    public List<WeatherSnapshot> forecast() {
        // TODO 11.2c: GET {baseUrl}/forecast? (same parameters), toSnapshot() on each item

        try {
            OpenWeatherDto.Forecast forecastList = restClient.get()
                    .uri(uri -> uri.path("/forecast")
                            .queryParam("lat", props.lat())
                            .queryParam("lon", props.lon())
                            .queryParam("units", "metric")
                            .queryParam("appid", props.apiKey())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        int statusCode = res.getStatusCode().value();
                        throw new WeatherUnavailableException("OpenWeather returned HTTP" + statusCode);})
                    .body(OpenWeatherDto.Forecast.class);

            List<WeatherSnapshot> forecastSnapshot = forecastList.list().stream()
                        .map(item -> item.toSnapshot())
                        .toList();

            return forecastSnapshot;

        }catch (RestClientException e){
            throw new WeatherUnavailableException("Can not reach OpenWeather" + e.getMessage(), e);
        }



    }
}
