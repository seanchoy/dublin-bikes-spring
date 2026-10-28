package ie.dublinbikes.station.domain;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


// TODO 2.4: 让 Spring 能通过组件扫描找到这个类（Part 2）
@Component
public class StationFinder {

    /** Part 1.3 + 1.4：见练习册 Part 1 */
    public List<NearbyStation> findNearest(double lat, double lng, List<Station> stations,
                                           boolean requireBikes, boolean requireStands, int limit) {
        // TODO 1.3: 计算距离、按距离从近到远排序、只取前 limit 个。要求用 Stream 写，不要用 for 循环
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
        // TODO 1.4: 加上 requireBikes / requireStands 两个过滤条件
    }

    /** Part 1.5 */
    public Optional<Station> findByNumber(List<Station> stations, int number) {
        // TODO 1.5: 找到编号为 number 的站点；找不到返回 Optional.empty()
        Optional<Station> stationbyNumber = stations.stream()
                .filter(s -> s.number() == number)
                .findAny();
        return stationbyNumber;
    }
}
