package ie.dublinbikes.weather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import ie.dublinbikes.weather.openweather.OpenWeatherClient;
import ie.dublinbikes.weather.openweather.OpenWeatherDto;

/**
 * Part 11: the OpenWeather client and caching.
 * The test uses the simplest CacheManager (an in-memory map) to check that a repeated request does not reach OpenWeather again.
 */
@RestClientTest(OpenWeatherClient.class)
@Import({WeatherConfig.class, CacheConfig.class, Part11OpenWeatherClientTest.TestCacheConfig.class})
@TestPropertySource(properties = {
        "app.openweather.api-key=test-key",
        "app.openweather.base-url=https://ow.test/data/2.5"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class Part11OpenWeatherClientTest {

    static final String QUERY = "?lat=53.3498&lon=-6.2603&units=metric&appid=test-key";
    static final String CURRENT_URL = "https://ow.test/data/2.5/weather" + QUERY;
    static final String FORECAST_URL = "https://ow.test/data/2.5/forecast" + QUERY;

    @TestConfiguration
    static class TestCacheConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("currentWeather", "weatherForecast");
        }
    }

    @Autowired
    OpenWeatherClient client;

    @Autowired
    MockRestServiceServer server;

    static ClassPathResource fixture(String name) {
        return new ClassPathResource("fixtures/" + name);
    }

    @Test
    @DisplayName("11.1 Current weather: feels_like and \"1h\" in rain map correctly; dt is a timestamp in seconds")
    void mapsCurrentWeather() {
        server.expect(requestTo(CURRENT_URL))
                .andRespond(withSuccess(fixture("openweather-current.json"), MediaType.APPLICATION_JSON));

        WeatherSnapshot now = client.current();

        assertThat(now).isEqualTo(new WeatherSnapshot(Instant.parse("2025-10-28T18:00:00Z"),
                9.4, 7.1, 87, 1012, 5.1, "light rain", 0.42, 0.0));
    }

    @Test
    @DisplayName("11.1 Forecast: every item becomes a WeatherSnapshot; \"3h\" in rain and pop map correctly; 0 and an empty string when rain / weather are missing")
    void mapsForecast() {
        server.expect(requestTo(FORECAST_URL))
                .andRespond(withSuccess(fixture("openweather-forecast.json"), MediaType.APPLICATION_JSON));

        List<WeatherSnapshot> forecast = client.forecast();

        assertThat(forecast).hasSize(3);
        assertThat(forecast.get(0)).isEqualTo(new WeatherSnapshot(Instant.parse("2025-10-28T20:00:00Z"),
                9.0, 6.8, 88, 1013, 5.4, "light rain", 0.9, 0.62));
        assertThat(forecast.get(1).rainMm()).isZero();
        assertThat(forecast.get(1).rainProbability()).isEqualTo(0.2);
        assertThat(forecast.get(2).description()).isEmpty();
    }

    @Test
    @DisplayName("11.1 DTO toSnapshot: rainfall is 0 when rain is missing entirely")
    void currentWithoutRain() {
        var dto = new OpenWeatherDto.Current(1761674400L, new OpenWeatherDto.Main(9.4, 7.1, 87, 1012),
                new OpenWeatherDto.Wind(5.1), List.of(), null);

        assertThat(dto.toSnapshot().rainMm()).isZero();
        assertThat(dto.toSnapshot().description()).isEmpty();
    }

    @Test
    @DisplayName("11.2 Throws WeatherUnavailableException on errors")
    void errorsBecomeWeatherUnavailable() {
        server.expect(requestTo(CURRENT_URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.current())
                .isInstanceOf(WeatherUnavailableException.class)
                .hasMessageContaining("401");
    }

    @Test
    @DisplayName("11.3 Cache: two current() calls in a row reach OpenWeather only once")
    void cachesCurrentWeather() {
        server.expect(once(), requestTo(CURRENT_URL))
                .andRespond(withSuccess(fixture("openweather-current.json"), MediaType.APPLICATION_JSON));

        WeatherSnapshot first = client.current();
        WeatherSnapshot second = client.current();

        assertThat(second).isEqualTo(first);
        server.verify();
    }

    @Test
    @DisplayName("11.3 Cache: forecast() is cached too, separately from current()")
    void cachesForecastSeparately() {
        server.expect(once(), requestTo(FORECAST_URL))
                .andRespond(withSuccess(fixture("openweather-forecast.json"), MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo(CURRENT_URL))
                .andRespond(withSuccess(fixture("openweather-current.json"), MediaType.APPLICATION_JSON));

        client.forecast();
        client.forecast();
        client.current();

        server.verify();
    }
}
