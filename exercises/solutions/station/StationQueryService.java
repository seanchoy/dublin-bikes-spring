package ie.dublinbikes.station;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ie.dublinbikes.station.domain.NearbyStation;
import ie.dublinbikes.station.domain.Station;
import ie.dublinbikes.station.domain.StationFinder;
import ie.dublinbikes.station.persistence.AvailabilityEntity;
import ie.dublinbikes.station.persistence.AvailabilityRepository;
import ie.dublinbikes.station.persistence.StationEntity;
import ie.dublinbikes.station.persistence.StationRepository;

@Service
@Transactional(readOnly = true)
public class StationQueryService {

    private final StationRepository stationRepository;
    private final AvailabilityRepository availabilityRepository;
    private final StationFinder stationFinder;

    public StationQueryService(StationRepository stationRepository,
                               AvailabilityRepository availabilityRepository,
                               StationFinder stationFinder) {
        this.stationRepository = stationRepository;
        this.availabilityRepository = availabilityRepository;
        this.stationFinder = stationFinder;
    }

    public List<Station> listStations() {
        Map<Integer, AvailabilityEntity> latestByNumber = availabilityRepository.findLatestForAllStations().stream()
                .collect(Collectors.toMap(a -> a.getId().number(), Function.identity()));
        return stationRepository.findAll(Sort.by("number")).stream()
                .map(s -> toStation(s, latestByNumber.get(s.getNumber())))
                .toList();
    }

    public Station getStation(int number) {
        StationEntity station = stationRepository.findById(number)
                .orElseThrow(() -> new StationNotFoundException(number));
        return toStation(station, availabilityRepository.findLatest(number).orElse(null));
    }

    public List<NearbyStation> findNearest(double lat, double lng, int limit, boolean requireBikes) {
        return stationFinder.findNearest(lat, lng, listStations(), requireBikes, false, limit);
    }

    /** availability 可能为 null（站点还没有任何可用量记录），这时车和空位都算 0。 */
    private static Station toStation(StationEntity station, AvailabilityEntity availability) {
        if (availability == null) {
            return new Station(station.getNumber(), station.getName(), station.getAddress(),
                    station.getLat(), station.getLng(), station.getStatus(), 0, 0, station.getBikeStands());
        }
        return new Station(station.getNumber(), station.getName(), station.getAddress(),
                station.getLat(), station.getLng(), availability.getStatus(),
                availability.getAvailableBikes(), availability.getAvailableBikeStands(), station.getBikeStands());
    }
}
