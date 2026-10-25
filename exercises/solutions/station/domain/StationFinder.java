package ie.dublinbikes.station.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class StationFinder {

    public List<NearbyStation> findNearest(double lat, double lng, List<Station> stations,
                                           boolean requireBikes, boolean requireStands, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be >= 0");
        }
        boolean anyFilter = requireBikes || requireStands;
        return stations.stream()
                .filter(s -> !anyFilter || s.isOpen())
                .filter(s -> !requireBikes || s.availableBikes() > 0)
                .filter(s -> !requireStands || s.availableStands() > 0)
                .map(s -> new NearbyStation(s, Math.round(GeoUtils.haversineMeters(lat, lng, s.lat(), s.lng()))))
                .sorted(Comparator.comparingLong(NearbyStation::distanceMeters))
                .limit(limit)
                .toList();
    }

    public Optional<Station> findByNumber(List<Station> stations, int number) {
        return stations.stream()
                .filter(s -> s.number() == number)
                .findFirst();
    }
}
