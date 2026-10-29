package ie.dublinbikes.station;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import ie.dublinbikes.station.domain.NearbyStation;
import ie.dublinbikes.station.domain.Station;
import ie.dublinbikes.station.domain.StationFinder;
import ie.dublinbikes.station.persistence.AvailabilityEntity;
import ie.dublinbikes.station.persistence.AvailabilityRepository;
import ie.dublinbikes.station.persistence.StationEntity;
import ie.dublinbikes.station.persistence.StationRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Part 6.1: see workbook Part 6 */
// TODO 6.1: mark as a Service and run every method in a read-only transaction
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
        // TODO 6.1a: all stations ordered by number, each with its latest availability (use the Part 4.5 query, only once)
        List<StationEntity> stations = stationRepository.findAll(Sort.by("number"));
        Map<Integer, AvailabilityEntity> latestByNumber;
        latestByNumber = availabilityRepository.findLatestForAllStations().stream()
                .collect(Collectors.toMap(
                        a -> a.getId().number(),
                        a -> a
                ));
        return stations.stream()
                .map(s -> toStation(s, latestByNumber.get(s.getNumber())))
                .toList();
    }

    public Station getStation(int number) {
        // TODO 6.1b: throw StationNotFoundException if the station does not exist
        Optional<StationEntity> stationEntity = stationRepository.findById(number);
        if(stationEntity.isEmpty()){
            throw new StationNotFoundException(number);
        }
        AvailabilityEntity latest = availabilityRepository.findLatest(number).orElse(null);
        return toStation(stationEntity.get(), latest);
    }

    public List<NearbyStation> findNearest(double lat, double lng, int limit, boolean requireBikes) {
        // TODO 6.1c: reuse StationFinder (pass false for requireStands)
        List<Station> allStations = listStations();
        List<NearbyStation> nearbyStations = stationFinder.findNearest(lat, lng, allStations, requireBikes, false, limit);
        return nearbyStations;
    }

    /** availability may be null (the station has no availability records yet); then bikes and stands count as 0. Provided. */
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
