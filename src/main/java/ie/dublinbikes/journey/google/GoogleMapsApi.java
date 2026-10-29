package ie.dublinbikes.journey.google;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

/**
 * Declarative HTTP client for Google Maps: just the interface; Spring generates the implementation at runtime.
 *
 * Part 14.1: see workbook Part 14
 */
public interface GoogleMapsApi {

    // TODO 14.1: GET /geocode/json?address=..&key=..
    @GetExchange("/geocode/json")
    GoogleDto.GeocodeResponse geocode(
            @RequestParam("address") String address,
            @RequestParam("key") String key);

    // TODO 14.1: GET /directions/json?origin=..&destination=..&mode=..&key=..
    @GetExchange("/directions/json")
    GoogleDto.DirectionsResponse directions(
            @RequestParam("origin") String origin,
            @RequestParam("destination") String destination,
            @RequestParam("mode") String mode,
            @RequestParam("key") String key);

}
