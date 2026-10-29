package ie.dublinbikes.journey.google;

import ie.dublinbikes.journey.*;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.util.List;

/** Part 14.3: see workbook Part 14 */
// TODO 14.3: mark as a Service
@Service
public class GoogleMapsService {

    private final GoogleMapsApi api;
    private final GoogleMapsProperties properties;

    public GoogleMapsService(GoogleMapsApi api, GoogleMapsProperties properties) {
        this.api = api;
        this.properties = properties;
    }

    public LatLng geocode(String address) {
        // TODO 14.3a: append ", Dublin, Ireland" to the address, call api.geocode with properties.serverKey(), return the first result's coordinates
        // TODO 14.3c: ZERO_RESULTS → ADDRESS_NOT_FOUND; any other non-OK status or HTTP error → ROUTE_UNAVAILABLE
        try{
            GoogleDto.GeocodeResponse geocodeResponse = api.geocode(address + ", Dublin, Ireland", properties.serverKey());

            if("ZERO_RESULTS".equals(geocodeResponse.status())){
                throw new JourneyException(JourneyException.Reason.ADDRESS_NOT_FOUND,"ADDRESS_NOT_FOUNT");
            }

            if(!"OK".equals((geocodeResponse.status()))){
                throw new JourneyException(JourneyException.Reason.ROUTE_UNAVAILABLE, geocodeResponse.status());
            }

            if(geocodeResponse.results().isEmpty()){
                throw new JourneyException(JourneyException.Reason.ROUTE_UNAVAILABLE, geocodeResponse.status());
            }


            GoogleDto.Location location = geocodeResponse.results().get(0).geometry().location();
            return new LatLng(location.lat(), location.lng());
        }catch (RestClientException e) {
            throw new JourneyException(JourneyException.Reason.ROUTE_UNAVAILABLE, "Google Map is unavailable: " +e.getMessage());
        }

    }

    public RouteLeg directions(LatLng from, LatLng to, TravelMode mode) {
        // TODO 14.3b: call api.directions (using LatLng.toParam() and mode.param()), take the first leg of the first route, convert it to a RouteLeg
        // TODO 14.3c: non-OK status or HTTP error → ROUTE_UNAVAILABLE
        try{
            GoogleDto.DirectionsResponse directionsResponse = api.directions(from.toParam(), to.toParam(), mode.param(), properties.serverKey());
            List<GoogleDto.Route> routes = directionsResponse.routes();

            if(!"OK".equals(directionsResponse.status()) || routes.isEmpty()){
                throw new JourneyException(JourneyException.Reason.ROUTE_UNAVAILABLE, directionsResponse.status());
            }

            GoogleDto.Leg leg = routes.get(0).legs().get(0);
            return new RouteLeg(mode, leg.distance().value(), leg.duration().value(), leg.distance().text(), leg.duration().text(), routes.get(0).overviewPolyline().points());
        }catch (RestClientException e){

            throw new JourneyException(JourneyException.Reason.ROUTE_UNAVAILABLE, "Google Map is unavaliable: " + e.getMessage());
        }


    }
}
