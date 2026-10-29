package ie.dublinbikes.journey.web;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ie.dublinbikes.journey.GoogleMapsProperties;
import ie.dublinbikes.journey.JourneyService;

@RestController
@RequestMapping("/api/v1/journeys")
public class JourneyController {

    private final JourneyService journeyService;
    private final GoogleMapsProperties mapsProperties;

    public JourneyController(JourneyService journeyService, GoogleMapsProperties mapsProperties) {
        this.journeyService = journeyService;
        this.mapsProperties = mapsProperties;
    }

    @PostMapping
    public JourneyResponse plan(@Valid @RequestBody JourneyRequest request) {
        return JourneyResponse.from(journeyService.plan(request.start(), request.end()));
    }

    /** 前端加载 Google 地图需要浏览器 key。已经写好。 */
    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of("googleMapsApiKey", mapsProperties.browserKey() == null ? "" : mapsProperties.browserKey());
    }
}
