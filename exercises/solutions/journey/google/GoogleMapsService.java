package ie.dublinbikes.journey.google;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import ie.dublinbikes.journey.GoogleMapsProperties;
import ie.dublinbikes.journey.JourneyException;
import ie.dublinbikes.journey.JourneyException.Reason;
import ie.dublinbikes.journey.LatLng;
import ie.dublinbikes.journey.RouteLeg;
import ie.dublinbikes.journey.TravelMode;

@Service
public class GoogleMapsService {

    private final GoogleMapsApi api;
    private final GoogleMapsProperties properties;

    public GoogleMapsService(GoogleMapsApi api, GoogleMapsProperties properties) {
        this.api = api;
        this.properties = properties;
    }

    public LatLng geocode(String address) {
        GoogleDto.GeocodeResponse response = call(() -> api.geocode(address + ", Dublin, Ireland", properties.serverKey()));
        if ("ZERO_RESULTS".equals(response.status()) || ("OK".equals(response.status()) && response.results().isEmpty())) {
            throw new JourneyException(Reason.ADDRESS_NOT_FOUND, "Could not find address: " + address);
        }
        if (!"OK".equals(response.status())) {
            throw new JourneyException(Reason.ROUTE_UNAVAILABLE, "Google Maps returned " + response.status());
        }
        GoogleDto.Location location = response.results().get(0).geometry().location();
        return new LatLng(location.lat(), location.lng());
    }

    public RouteLeg directions(LatLng from, LatLng to, TravelMode mode) {
        GoogleDto.DirectionsResponse response = call(() ->
                api.directions(from.toParam(), to.toParam(), mode.param(), properties.serverKey()));
        if (!"OK".equals(response.status()) || response.routes().isEmpty()) {
            throw new JourneyException(Reason.ROUTE_UNAVAILABLE, "No route found (status: " + response.status() + ")");
        }
        GoogleDto.Route route = response.routes().get(0);
        GoogleDto.Leg leg = route.legs().get(0);
        return new RouteLeg(mode, leg.distance().value(), leg.duration().value(),
                leg.distance().text(), leg.duration().text(), route.overviewPolyline().points());
    }

    private static <T> T call(java.util.function.Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientException e) {
            throw new JourneyException(Reason.ROUTE_UNAVAILABLE, "Google Maps is unavailable: " + e.getMessage());
        }
    }
}
