package ie.dublinbikes.forecast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

/**
 * Part 7: calling the Python inference service (POST + JSON contract).
 * MockRestServiceServer stands in for the Python service: it checks the request body you send and returns a prepared response.
 */
@RestClientTest(ForecastClient.class)
@Import(ForecastPropertiesConfig.class)
@TestPropertySource(properties = "app.forecast.base-url=http://forecast.test")
class Part7ForecastClientTest {

    static final String URL = "http://forecast.test/predict";
    static final List<LocalDateTime> TIMES = List.of(
            LocalDateTime.parse("2026-10-28T18:00:00"), LocalDateTime.parse("2026-10-28T19:00:00"));

    @Autowired
    ForecastClient client;

    @Autowired
    MockRestServiceServer server;

    static ClassPathResource fixture() {
        return new ClassPathResource("fixtures/forecast-response.json");
    }

    @Test
    @DisplayName("7.1 The request body is snake_case JSON: station_id, times, weather[].avg_temp")
    void serializesRequestAsSnakeCase() {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.station_id").value(42))
                .andExpect(jsonPath("$.capacity").value(30))
                .andExpect(jsonPath("$.times[0]").value("2026-10-28T18:00:00"))
                .andExpect(jsonPath("$.times.length()").value(2))
                .andExpect(jsonPath("$.weather[0].avg_temp").value(9.0))
                .andExpect(jsonPath("$.weather[0].avg_humidity").value(82.0))
                .andExpect(jsonPath("$.weather[0].avg_pressure").value(1010.0))
                .andExpect(jsonPath("$.weather[0].is_raining").value(1))
                .andRespond(withSuccess(fixture(), MediaType.APPLICATION_JSON));

        WeatherInput rainy = new WeatherInput(9.0, 82.0, 1010.0, 1);
        client.predict(new ForecastRequest(42, 30, TIMES, List.of(rainy, rainy)));

        server.verify();
    }

    @Test
    @DisplayName("7.1 snake_case fields in the response map to HourlyPrediction")
    void deserializesResponse() {
        server.expect(requestTo(URL)).andRespond(withSuccess(fixture(), MediaType.APPLICATION_JSON));

        List<HourlyPrediction> result = client.predict(new ForecastRequest(42, 30, TIMES, null));

        assertThat(result).hasSize(3);
        HourlyPrediction first = result.get(0);
        assertThat(first.time()).isEqualTo(LocalDateTime.parse("2026-10-28T18:00:00"));
        assertThat(first.bikes()).isEqualTo(12.4);
        assertThat(first.docks()).isEqualTo(17.6);
        assertThat(first.avgTemp()).isEqualTo(8.0);
        assertThat(first.avgHumidity()).isEqualTo(83.5);
        assertThat(first.avgPressure()).isEqualTo(1014.5);
        assertThat(first.isRaining()).isZero();
        assertThat(result.get(2).isRaining()).isEqualTo(1);
    }

    @Test
    @DisplayName("7.3 When weather is null, the request body has no weather field at all")
    void omitsNullWeather() {
        server.expect(requestTo(URL))
                .andExpect(jsonPath("$.station_id").value(42))
                .andExpect(jsonPath("$.weather").doesNotExist())
                .andRespond(withSuccess(fixture(), MediaType.APPLICATION_JSON));

        client.predict(new ForecastRequest(42, 30, TIMES, null));

        server.verify();
    }

    @Test
    @DisplayName("7.2 Service returns 503: ForecastUnavailableException, status = 503, retryable")
    void serverErrorIsRetryable() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> client.predict(new ForecastRequest(42, 30, TIMES, null)))
                .isInstanceOfSatisfying(ForecastUnavailableException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(503);
                    assertThat(e.isRetryable()).isTrue();
                    assertThat(e).hasMessageContaining("503");
                });
    }

    @Test
    @DisplayName("7.2 Service returns 422 (invalid request): status = 422, not worth retrying")
    void clientErrorIsNotRetryable() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.UNPROCESSABLE_CONTENT));

        assertThatThrownBy(() -> client.predict(new ForecastRequest(42, 30, TIMES, null)))
                .isInstanceOfSatisfying(ForecastUnavailableException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(422);
                    assertThat(e.isRetryable()).isFalse();
                });
    }

    @Test
    @DisplayName("7.2 Network error: status = 0, retryable, original exception kept as the cause")
    void networkErrorIsRetryable() {
        server.expect(requestTo(URL)).andRespond(withException(new IOException("connection refused")));

        assertThatThrownBy(() -> client.predict(new ForecastRequest(42, 30, TIMES, null)))
                .isInstanceOfSatisfying(ForecastUnavailableException.class, e -> {
                    assertThat(e.getStatus()).isZero();
                    assertThat(e.isRetryable()).isTrue();
                    assertThat(e.getCause()).isNotNull();
                });
    }
}
