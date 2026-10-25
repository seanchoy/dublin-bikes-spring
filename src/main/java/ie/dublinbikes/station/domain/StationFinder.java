package ie.dublinbikes.station.domain;

import java.util.List;
import java.util.Optional;

// TODO 2.4: 让 Spring 能通过组件扫描找到这个类（Part 2）
public class StationFinder {

    /** Part 1.3 + 1.4：见练习册 Part 1 */
    public List<NearbyStation> findNearest(double lat, double lng, List<Station> stations,
                                           boolean requireBikes, boolean requireStands, int limit) {
        // TODO 1.3: 计算距离、按距离从近到远排序、只取前 limit 个。要求用 Stream 写，不要用 for 循环
        // TODO 1.4: 加上 requireBikes / requireStands 两个过滤条件
        throw new UnsupportedOperationException("TODO 1.3");
    }

    /** Part 1.5 */
    public Optional<Station> findByNumber(List<Station> stations, int number) {
        // TODO 1.5: 找到编号为 number 的站点；找不到返回 Optional.empty()
        throw new UnsupportedOperationException("TODO 1.5");
    }
}
