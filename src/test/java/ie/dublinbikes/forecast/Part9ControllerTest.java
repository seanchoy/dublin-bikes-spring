package ie.dublinbikes.forecast;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ie.dublinbikes.station.StationNotFoundException;

/** Part 9.2: the forecast endpoint. */
@WebMvcTest(StationForecastController.class)
class Part9ControllerTest {

    static final StationForecast FORECAST = new StationForecast(42, "SMITHFIELD NORTH", 30, 3, false, List.of(
            new HourlyPrediction(LocalDateTime.parse("2026-10-28T18:00:00"), 12.4, 17.6, 8.0, 83.5, 1014.5, 0),
            new HourlyPrediction(LocalDateTime.parse("2026-10-28T19:00:00"), 15.5, 14.5, 7.9, 84.2, 1014.7, 1)));

    @Autowired
    MockMvc mvc;

    @MockitoBean
    StationForecastService forecastService;

    @Test
    @DisplayName("9.2 GET /api/v1/stations/42/forecast: 8 hours by default, camelCase JSON")
    void forecastDefaults() throws Exception {
        when(forecastService.forecast(42, 8)).thenReturn(FORECAST);

        mvc.perform(get("/api/v1/stations/42/forecast"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.number").value(42))
                .andExpect(jsonPath("$.name").value("SMITHFIELD NORTH"))
                .andExpect(jsonPath("$.capacity").value(30))
                .andExpect(jsonPath("$.currentBikes").value(3))
                .andExpect(jsonPath("$.degraded").value(false))
                .andExpect(jsonPath("$.predictions.length()").value(2))
                .andExpect(jsonPath("$.predictions[0].time").value("2026-10-28T18:00:00"))
                .andExpect(jsonPath("$.predictions[0].bikes").value(12.4))
                .andExpect(jsonPath("$.predictions[0].temperature").value(8.0))
                .andExpect(jsonPath("$.predictions[0].raining").value(false))
                .andExpect(jsonPath("$.predictions[1].raining").value(true))
                .andExpect(jsonPath("$.predictions[0].avg_temp").doesNotExist());
    }

    @Test
    @DisplayName("9.2 hours=24 is passed to the Service unchanged")
    void forecastWithHours() throws Exception {
        when(forecastService.forecast(42, 24)).thenReturn(FORECAST);

        mvc.perform(get("/api/v1/stations/42/forecast").param("hours", "24"))
                .andExpect(status().isOk());

        verify(forecastService).forecast(42, 24);
    }

    @Test
    @DisplayName("9.3 hours outside 1 to 48: 400, the Service is not called")
    void invalidHours() throws Exception {
        mvc.perform(get("/api/v1/stations/42/forecast").param("hours", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/stations/42/forecast").param("hours", "49")).andExpect(status().isBadRequest());

        verify(forecastService, never()).forecast(anyInt(), anyInt());
    }

    @Test
    @DisplayName("9.3 Station does not exist: 404 ProblemDetail (handled by the Part 6 GlobalExceptionHandler)")
    void stationNotFound() throws Exception {
        when(forecastService.forecast(999, 8)).thenThrow(new StationNotFoundException(999));

        mvc.perform(get("/api/v1/stations/999/forecast"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Station not found"));
    }

    @Test
    @DisplayName("9.2 When degraded it still returns 200, degraded = true and an empty predictions array")
    void degraded() throws Exception {
        when(forecastService.forecast(42, 8))
                .thenReturn(new StationForecast(42, "SMITHFIELD NORTH", 30, 3, true, List.of()));

        mvc.perform(get("/api/v1/stations/42/forecast"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.degraded").value(true))
                .andExpect(jsonPath("$.currentBikes").value(3))
                .andExpect(jsonPath("$.predictions.length()").value(0));
    }
}
