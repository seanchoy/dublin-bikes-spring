package ie.dublinbikes.journey;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ie.dublinbikes.journey.JourneyException.Reason;
import ie.dublinbikes.journey.web.JourneyController;
import ie.dublinbikes.station.domain.NearbyStation;
import ie.dublinbikes.station.domain.Station;

/** Part 15.2 / 15.3: the POST endpoint, request body validation and error codes. */
@WebMvcTest(JourneyController.class)
@Import(Part15ControllerTest.Props.class)
@TestPropertySource(properties = "app.google.browser-key=browser-key")
class Part15ControllerTest {

    @TestConfiguration
    @EnableConfigurationProperties(GoogleMapsProperties.class)
    static class Props {
    }

    static final RouteLeg WALK = new RouteLeg(TravelMode.WALKING, 800, 600, "0.8 km", "10 mins", "w1");
    static final RouteLeg CYCLE = new RouteLeg(TravelMode.BICYCLING, 1700, 400, "1.7 km", "7 mins", "c1");
    static final RouteLeg WALK2 = new RouteLeg(TravelMode.WALKING, 100, 80, "0.1 km", "1 min", "w2");
    static final JourneyPlan PLAN = new JourneyPlan(new LatLng(53.3498, -6.2603), new LatLng(53.331, -6.26),
            new NearbyStation(new Station(10, "DAME STREET", "Dame Street", 53.344007, -6.266802, "OPEN", 5, 0, 16), 775),
            new NearbyStation(new Station(5, "CHARLEMONT PLACE", "Charlemont Place", 53.330662, -6.260177, "OPEN", 10, 30, 40), 40),
            WALK, CYCLE, WALK2);

    @Autowired
    MockMvc mvc;

    @MockitoBean
    JourneyService journeyService;

    static final String BODY = "{\"start\":\"O'Connell Street\",\"end\":\"Grand Canal\"}";

    @Test
    @DisplayName("15.2 POST /api/v1/journeys: the body becomes a JourneyRequest and the whole journey is returned")
    void plan() throws Exception {
        when(journeyService.plan("O'Connell Street", "Grand Canal")).thenReturn(PLAN);

        mvc.perform(post("/api/v1/journeys").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pickup.number").value(10))
                .andExpect(jsonPath("$.pickup.distanceMeters").value(775))
                .andExpect(jsonPath("$.dropoff.number").value(5))
                .andExpect(jsonPath("$.cycle.mode").value("BICYCLING"))
                .andExpect(jsonPath("$.cycle.polyline").value("c1"))
                .andExpect(jsonPath("$.totalDistanceMeters").value(2600))
                .andExpect(jsonPath("$.totalDurationSeconds").value(1080));
    }

    @Test
    @DisplayName("15.3 start is an empty string: 400, the Service is not called")
    void blankStart() throws Exception {
        mvc.perform(post("/api/v1/journeys").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"start\":\"  \",\"end\":\"Grand Canal\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        verify(journeyService, never()).plan(anyString(), anyString());
    }

    @Test
    @DisplayName("15.3 No request body: 400")
    void missingBody() throws Exception {
        mvc.perform(post("/api/v1/journeys").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("15.3 Address not found: 404 with reason")
    void addressNotFound() throws Exception {
        when(journeyService.plan(anyString(), anyString()))
                .thenThrow(new JourneyException(Reason.ADDRESS_NOT_FOUND, "Could not find address: Nowhere"));

        mvc.perform(post("/api/v1/journeys").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Journey not available"))
                .andExpect(jsonPath("$.reason").value("ADDRESS_NOT_FOUND"));
    }

    @Test
    @DisplayName("15.3 Google error: 502 Bad Gateway")
    void upstreamError() throws Exception {
        when(journeyService.plan(anyString(), anyString()))
                .thenThrow(new JourneyException(Reason.ROUTE_UNAVAILABLE, "Google Maps returned REQUEST_DENIED"));

        mvc.perform(post("/api/v1/journeys").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.reason").value("ROUTE_UNAVAILABLE"));
    }

    @Test
    @DisplayName("GET /api/v1/journeys/config: returns the browser key for the frontend (provided; checks it works)")
    void config() throws Exception {
        mvc.perform(get("/api/v1/journeys/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.googleMapsApiKey").value("browser-key"));
    }
}
