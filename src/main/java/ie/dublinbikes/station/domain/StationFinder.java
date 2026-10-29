package ie.dublinbikes.station.domain;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


// TODO 2.4: let Spring find this class through component scanning (Part 2)
@Component
public class StationFinder {

    /** Part 1.3 + 1.4: see workbook Part 1 */
    public List<NearbyStation> findNearest(double lat, double lng, List<Station> stations,
                                           boolean requireBikes, boolean requireStands, int limit) {
        // TODO 1.3: compute distances, sort nearest first and keep only the first limit. Use a Stream, not a for loop
        if(limit < 0){
            throw new IllegalArgumentException("invalid: limit < 0");
        }
        List<NearbyStation> nearbyStation = stations.stream()
                .filter(s -> (!requireBikes && !requireStands) || s.isOpen())
                .filter(s -> !requireBikes || s.availableBikes() > 0)
                .filter(s -> !requireStands || s.availableStands() > 0)
                .map(s -> new NearbyStation(s, Math.round(GeoUtils.haversineMeters(s.lat(), s.lng(), lat, lng))))
                .sorted(Comparator.comparingLong(NearbyStation::distanceMeters))
                .limit(limit)
                .toList();
        return nearbyStation;
        // TODO 1.4: add the requireBikes / requireStands filters
    }

    /** Part 1.5 */
    public Optional<Station> findByNumber(List<Station> stations, int number) {
        // TODO 1.5: find the station with this number; return Optional.empty() if there is none
        Optional<Station> stationbyNumber = stations.stream()
                .filter(s -> s.number() == number)
                .findAny();
        return stationbyNumber;
    }
}
