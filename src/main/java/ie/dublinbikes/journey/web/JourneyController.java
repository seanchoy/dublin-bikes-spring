package ie.dublinbikes.journey.web;

import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import ie.dublinbikes.journey.GoogleMapsProperties;
import ie.dublinbikes.journey.JourneyService;

/** Part 15.2 / 15.3: see workbook Part 15 */
// TODO 15.2: REST controller with base path /api/v1/journeys
@RestController
@RequestMapping("/api/v1/journeys")
public class JourneyController {

    private final JourneyService journeyService;
    private final GoogleMapsProperties mapsProperties;

    public JourneyController(JourneyService journeyService, GoogleMapsProperties mapsProperties) {
        this.journeyService = journeyService;
        this.mapsProperties = mapsProperties;
    }

    // TODO 15.2: POST /api/v1/journeys; the parameter comes from the JSON body
    // TODO 15.3: trigger validation on JourneyRequest
    @PostMapping
    public JourneyResponse plan(@Valid @RequestBody JourneyRequest request) {
        JourneyResponse response = JourneyResponse.from(journeyService.plan(request.start(), request.end()));
        return response;
    }

    /** The frontend needs the browser key to load Google Maps. Provided. */
    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of("googleMapsApiKey", mapsProperties.browserKey() == null ? "" : mapsProperties.browserKey());
    }
}
