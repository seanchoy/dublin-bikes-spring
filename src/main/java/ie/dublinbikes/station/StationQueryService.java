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

/** Part 6.1：见练习册 Part 6 */
// TODO 6.1: 标记成 Service，并让所有方法都在只读事务里执行
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
        // TODO 6.1a: 所有站点按 number 升序，每个站点配上它最新的可用量（用 Part 4.5 的查询，只查一次）
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
        // TODO 6.1b: 站点不存在时抛 StationNotFoundException
        Optional<StationEntity> stationEntity = stationRepository.findById(number);
        if(stationEntity.isEmpty()){
            throw new StationNotFoundException(number);
        }
        AvailabilityEntity latest = availabilityRepository.findLatest(number).orElse(null);
        return toStation(stationEntity.get(), latest);
    }

    public List<NearbyStation> findNearest(double lat, double lng, int limit, boolean requireBikes) {
        // TODO 6.1c: 复用 StationFinder（requireStands 传 false）
        List<Station> allStations = listStations();
        List<NearbyStation> nearbyStations = stationFinder.findNearest(lat, lng, allStations, requireBikes, false, limit);
        return nearbyStations;
    }

    /** availability 可能为 null（站点还没有任何可用量记录），这时车和空位都算 0。这个方法已经写好了。 */
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
