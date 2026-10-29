package ie.dublinbikes.journey.google;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface GoogleMapsApi {

    @GetExchange("/geocode/json")
    GoogleDto.GeocodeResponse geocode(@RequestParam("address") String address, @RequestParam("key") String key);

    @GetExchange("/directions/json")
    GoogleDto.DirectionsResponse directions(@RequestParam("origin") String origin,
                                            @RequestParam("destination") String destination,
                                            @RequestParam("mode") String mode,
                                            @RequestParam("key") String key);
}
