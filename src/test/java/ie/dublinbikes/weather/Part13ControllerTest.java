package ie.dublinbikes.weather;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ie.dublinbikes.weather.web.WeatherController;

/** Part 13.2: the weather endpoints and 503 error handling. */
@WebMvcTest(WeatherController.class)
class Part13ControllerTest {

    static final WeatherSnapshot NOW =
            new WeatherSnapshot(Instant.parse("2026-10-28T18:00:00Z"), 9.4, 7.1, 87, 1012, 5.1, "light rain", 0.42, 0.0);

    @Autowired
    MockMvc mvc;

    @MockitoBean
    WeatherQueryService weatherService;

    @Test
    @DisplayName("13.2 GET /api/v1/weather/current")
    void current() throws Exception {
        when(weatherService.current()).thenReturn(NOW);

        mvc.perform(get("/api/v1/weather/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.time").value("2026-10-28T18:00:00Z"))
                .andExpect(jsonPath("$.temperature").value(9.4))
                .andExpect(jsonPath("$.feelsLike").value(7.1))
                .andExpect(jsonPath("$.windSpeed").value(5.1))
                .andExpect(jsonPath("$.description").value("light rain"))
                .andExpect(jsonPath("$.rainMm").value(0.42));
    }

    @Test
    @DisplayName("13.2 GET /api/v1/weather/forecast: limit defaults to 8")
    void forecastDefaultLimit() throws Exception {
        when(weatherService.forecast(8)).thenReturn(List.of(NOW, NOW));

        mvc.perform(get("/api/v1/weather/forecast"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].temperature").value(9.4));
        verify(weatherService).forecast(8);
    }

    @Test
    @DisplayName("13.2 limit outside 1 to 40: 400")
    void invalidLimit() throws Exception {
        mvc.perform(get("/api/v1/weather/forecast").param("limit", "41")).andExpect(status().isBadRequest());
        verify(weatherService, never()).forecast(anyInt());
    }

    @Test
    @DisplayName("13.2 No weather available: 503 ProblemDetail with title \"Weather unavailable\"")
    void unavailable() throws Exception {
        when(weatherService.current()).thenThrow(new WeatherUnavailableException("OpenWeather returned HTTP 503"));

        mvc.perform(get("/api/v1/weather/current"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Weather unavailable"))
                .andExpect(jsonPath("$.detail").value("OpenWeather returned HTTP 503"));
    }
}
