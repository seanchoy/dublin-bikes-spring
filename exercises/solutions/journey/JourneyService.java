package ie.dublinbikes.journey;

import java.util.List;

import org.springframework.stereotype.Service;

import ie.dublinbikes.journey.JourneyException.Reason;
import ie.dublinbikes.journey.google.GoogleMapsService;
import ie.dublinbikes.station.StationQueryService;
import ie.dublinbikes.station.domain.NearbyStation;
import ie.dublinbikes.station.domain.Station;
import ie.dublinbikes.station.domain.StationFinder;

@Service
public class JourneyService {

    private final GoogleMapsService maps;
    private final StationQueryService stationQueryService;
    private final StationFinder stationFinder;

    public JourneyService(GoogleMapsService maps, StationQueryService stationQueryService, StationFinder stationFinder) {
        this.maps = maps;
        this.stationQueryService = stationQueryService;
        this.stationFinder = stationFinder;
    }

    public JourneyPlan plan(String startAddress, String endAddress) {
        LatLng start = maps.geocode(startAddress);
        LatLng end = maps.geocode(endAddress);
        List<Station> stations = stationQueryService.listStations();

        NearbyStation pickup = nearest(start, stations, true, false, "No station with bikes near the start");
        NearbyStation dropoff = nearest(end, stations, false, true, "No station with free stands near the destination");

        LatLng pickupAt = new LatLng(pickup.station().lat(), pickup.station().lng());
        LatLng dropoffAt = new LatLng(dropoff.station().lat(), dropoff.station().lng());
        return new JourneyPlan(start, end, pickup, dropoff,
                maps.directions(start, pickupAt, TravelMode.WALKING),
                maps.directions(pickupAt, dropoffAt, TravelMode.BICYCLING),
                maps.directions(dropoffAt, end, TravelMode.WALKING));
    }

    private NearbyStation nearest(LatLng point, List<Station> stations, boolean requireBikes, boolean requireStands,
                                  String message) {
        return stationFinder.findNearest(point.lat(), point.lng(), stations, requireBikes, requireStands, 1)
                .stream().findFirst()
                .orElseThrow(() -> new JourneyException(Reason.NO_STATION_AVAILABLE, message));
    }
}
