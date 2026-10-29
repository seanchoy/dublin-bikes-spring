package ie.dublinbikes.station.web;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ie.dublinbikes.station.StationNotFoundException;
import ie.dublinbikes.station.StationQueryService;
import ie.dublinbikes.station.domain.NearbyStation;
import ie.dublinbikes.station.domain.Station;

/**
 * Part 6.2 / 6.3: REST controllers and unified error handling.
 * WebMvcTest starts only the web layer (controllers, controller advice, JSON conversion), no database.
 * The Service is replaced with a fake via @MockitoBean, and MockMvc simulates HTTP requests without opening a port.
 */
@WebMvcTest(StationController.class)
class Part6ControllerTest {

    static final Station SMITHFIELD = new Station(42, "SMITHFIELD NORTH", "Smithfield North",
            53.349562, -6.278198, "OPEN", 3, 27, 30);

    @Autowired
    MockMvc mvc;

    @MockitoBean
    StationQueryService queryService;

    @Test
    @DisplayName("6.2 GET /api/v1/stations returns a JSON array with camelCase field names")
    void listStations() throws Exception {
        when(queryService.listStations()).thenReturn(List.of(SMITHFIELD));

        mvc.perform(get("/api/v1/stations"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].number").value(42))
                .andExpect(jsonPath("$[0].name").value("SMITHFIELD NORTH"))
                .andExpect(jsonPath("$[0].lat").value(53.349562))
                .andExpect(jsonPath("$[0].availableBikes").value(3))
                .andExpect(jsonPath("$[0].availableStands").value(27))
                .andExpect(jsonPath("$[0].totalStands").value(30));
    }

    @Test
    @DisplayName("6.2 GET /api/v1/stations/42 returns a single station")
    void getStation() throws Exception {
        when(queryService.getStation(42)).thenReturn(SMITHFIELD);

        mvc.perform(get("/api/v1/stations/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(42))
                .andExpect(jsonPath("$.availableBikes").value(3));
    }

    @Test
    @DisplayName("6.3 A missing station returns a 404 ProblemDetail")
    void stationNotFound() throws Exception {
        when(queryService.getStation(999)).thenThrow(new StationNotFoundException(999));

        mvc.perform(get("/api/v1/stations/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Station not found"))
                .andExpect(jsonPath("$.detail").value("Station 999 does not exist"))
                .andExpect(jsonPath("$.stationNumber").value(999));
    }

    @Test
    @DisplayName("6.2 GET /nearest: without limit and requireBikes, defaults to 3 and false")
    void nearestDefaults() throws Exception {
        when(queryService.findNearest(53.3498, -6.2603, 3, false))
                .thenReturn(List.of(new NearbyStation(SMITHFIELD, 1188)));

        mvc.perform(get("/api/v1/stations/nearest").param("lat", "53.3498").param("lng", "-6.2603"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].number").value(42))
                .andExpect(jsonPath("$[0].availableBikes").value(3))
                .andExpect(jsonPath("$[0].distanceMeters").value(1188));

        verify(queryService).findNearest(53.3498, -6.2603, 3, false);
    }

    @Test
    @DisplayName("6.2 GET /nearest: with limit and requireBikes")
    void nearestWithParams() throws Exception {
        when(queryService.findNearest(53.3498, -6.2603, 5, true)).thenReturn(List.of());

        mvc.perform(get("/api/v1/stations/nearest")
                        .param("lat", "53.3498").param("lng", "-6.2603")
                        .param("limit", "5").param("requireBikes", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(queryService).findNearest(53.3498, -6.2603, 5, true);
    }

    @Test
    @DisplayName("6.3 lat outside -90 to 90 returns a 400 ProblemDetail, and the Service is not called")
    void invalidLatitude() throws Exception {
        mvc.perform(get("/api/v1/stations/nearest").param("lat", "100").param("lng", "-6.2603"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        verify(queryService, never()).findNearest(anyDouble(), anyDouble(), anyInt(), anyBoolean());
    }

    @Test
    @DisplayName("6.3 limit outside 1 to 20 returns 400")
    void invalidLimit() throws Exception {
        mvc.perform(get("/api/v1/stations/nearest").param("lat", "53.3").param("lng", "-6.2").param("limit", "50"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("6.3 Missing required parameter lat returns a 400 ProblemDetail")
    void missingLatitude() throws Exception {
        mvc.perform(get("/api/v1/stations/nearest").param("lng", "-6.2603"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }
}
