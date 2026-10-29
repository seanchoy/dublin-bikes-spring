package ie.dublinbikes.journey;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ie.dublinbikes.journey.JourneyException.Reason;
import ie.dublinbikes.journey.web.PredictiveJourneyController;
import ie.dublinbikes.station.domain.NearbyStation;
import ie.dublinbikes.station.domain.Station;

/** POST /api/v1/journeys/predict: request body, response shape and error codes. */
@WebMvcTest(PredictiveJourneyController.class)
class Part15PredictiveJourneyControllerTest {

    static final LocalDateTime DEPART = LocalDateTime.parse("2026-10-29T08:00");
    static final RouteLeg WALK = new RouteLeg(TravelMode.WALKING, 800, 600, "0.8 km", "10 mins", "w1");
    static final RouteLeg CYCLE = new RouteLeg(TravelMode.BICYCLING, 1700, 400, "1.7 km", "7 mins", "c1");
    static final RouteLeg WALK2 = new RouteLeg(TravelMode.WALKING, 100, 80, "0.1 km", "1 min", "w2");
    static final JourneyPlan JOURNEY = new JourneyPlan(new LatLng(53.3498, -6.2603), new LatLng(53.331, -6.26),
            new NearbyStation(new Station(33, "PRINCES STREET", "Princes Street", 53.349013, -6.260311, "OPEN", 0, 20, 20), 88),
            new NearbyStation(new Station(23, "HARCOURT TERRACE", "Harcourt Terrace", 53.332763, -6.257942, "OPEN", 2, 0, 20), 230),
            WALK, CYCLE, WALK2);
    static final PredictedJourneyPlan PLAN = new PredictedJourneyPlan(JOURNEY, DEPART,
            new StationOutlook(DEPART, 6.2, 13.8, true), new StationOutlook(null, 2, 0, false));

    static final String BODY = """
            {"start": "O'Connell Street", "end": "Grand Canal", "departAt": "2026-10-29T08:00"}""";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PredictiveJourneyService journeyService;

    @Test
    @DisplayName("departAt is read as a LocalDateTime; the response has the journey and both station outlooks")
    void predict() throws Exception {
        when(journeyService.plan("O'Connell Street", "Grand Canal", DEPART)).thenReturn(PLAN);

        mvc.perform(post("/api/v1/journeys/predict").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departAt").value("2026-10-29T08:00:00"))
                .andExpect(jsonPath("$.journey.pickup.number").value(33))
                .andExpect(jsonPath("$.journey.dropoff.number").value(23))
                .andExpect(jsonPath("$.journey.totalDistanceMeters").value(2600))
                .andExpect(jsonPath("$.pickupOutlook.bikes").value(6.2))
                .andExpect(jsonPath("$.pickupOutlook.predicted").value(true))
                .andExpect(jsonPath("$.dropoffOutlook.predicted").value(false));
    }

    @Test
    @DisplayName("Missing departAt: 400 and the service is not called")
    void missingDepartAt() throws Exception {
        mvc.perform(post("/api/v1/journeys/predict").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"start\": \"O'Connell Street\", \"end\": \"Grand Canal\"}"))
                .andExpect(status().isBadRequest());
        verify(journeyService, never()).plan(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Departure outside the forecast range: 400 with title \"Invalid departure time\"")
    void invalidDeparture() throws Exception {
        when(journeyService.plan(anyString(), anyString(), any()))
                .thenThrow(new InvalidDepartureException("departAt must be between now and 48 hours from now"));

        mvc.perform(post("/api/v1/journeys/predict").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid departure time"));
    }

    @Test
    @DisplayName("No station expected nearby: 404 with reason, same as a normal journey (JourneyExceptionHandler)")
    void noStationAvailable() throws Exception {
        when(journeyService.plan(anyString(), anyString(), any()))
                .thenThrow(new JourneyException(Reason.NO_STATION_AVAILABLE, "No station is expected to have bikes"));

        mvc.perform(post("/api/v1/journeys/predict").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("NO_STATION_AVAILABLE"));
    }
}
