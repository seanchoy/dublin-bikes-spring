package ie.dublinbikes.journey;

import ie.dublinbikes.journey.google.GoogleDto;
import ie.dublinbikes.journey.google.GoogleMapsService;
import ie.dublinbikes.station.StationQueryService;
import ie.dublinbikes.station.domain.NearbyStation;
import ie.dublinbikes.station.domain.Station;
import ie.dublinbikes.station.domain.StationFinder;
import ie.dublinbikes.station.web.NearbyStationResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/** Part 15.1: see workbook Part 15 */
// TODO 15.1: mark as a Service
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
        // TODO 15.1: geocode both addresses → nearest station "with bikes" near the start, nearest station "with free stands" near the end
        //            (throw JourneyException(NO_STATION_AVAILABLE, ...) if none) → three legs: walk, cycle, walk

        LatLng start = maps.geocode(startAddress);
        LatLng end = maps.geocode(endAddress);

        List<Station> allStations = stationQueryService.listStations();

        List<NearbyStation> startNearByStation = stationFinder.findNearest(start.lat(), start.lng(), allStations, true, false, 1);
        List<NearbyStation> endNearByStation = stationFinder.findNearest(end.lat(), end.lng(), allStations, false, true, 1);

        if (startNearByStation.isEmpty()) {
            throw new JourneyException(JourneyException.Reason.NO_STATION_AVAILABLE,
                    "No station with bikes near the start");
        }
        if (endNearByStation.isEmpty()) {
            throw new JourneyException(JourneyException.Reason.NO_STATION_AVAILABLE,
                    "No station with free stands near the destination");
        }

        LatLng cycleStart = new LatLng(startNearByStation.get(0).station().lat(), startNearByStation.get(0).station().lng());
        LatLng cycleEnd = new LatLng(endNearByStation.get(0).station().lat(), endNearByStation.get(0).station().lng());

        RouteLeg leg_1 = maps.directions(start, cycleStart, TravelMode.WALKING);
        RouteLeg leg_2 = maps.directions(cycleStart, cycleEnd, TravelMode.BICYCLING);
        RouteLeg leg_3 = maps.directions(cycleEnd, end, TravelMode.WALKING);

        return new JourneyPlan(start, end, startNearByStation.get(0), endNearByStation.get(0), leg_1, leg_2, leg_3);


    }
}
