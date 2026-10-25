package ie.dublinbikes.station.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Part 1：现代 Java（record、Stream、Optional）。这一关不涉及 Spring。 */
class Part1ModernJavaTest {

    // 查询点：O'Connell Street 的 Spire
    static final double SPIRE_LAT = 53.3498;
    static final double SPIRE_LNG = -6.2603;

    static final Station PRINCES = new Station(33, "PRINCES STREET", "Princes Street", 53.349013, -6.260311, "OPEN", 0, 20, 20);
    static final Station DAME = new Station(10, "DAME STREET", "Dame Street", 53.344007, -6.266802, "OPEN", 5, 0, 16);
    static final Station BLESSINGTON = new Station(2, "BLESSINGTON STREET", "Blessington Street", 53.356769, -6.26814, "CLOSED", 7, 13, 20);
    static final Station SMITHFIELD = new Station(42, "SMITHFIELD NORTH", "Smithfield North", 53.349562, -6.278198, "OPEN", 3, 27, 30);
    static final Station CHARLEMONT = new Station(5, "CHARLEMONT PLACE", "Charlemont Place", 53.330662, -6.260177, "OPEN", 10, 30, 40);

    // 故意打乱顺序
    static final List<Station> STATIONS = List.of(SMITHFIELD, CHARLEMONT, PRINCES, BLESSINGTON, DAME);

    final StationFinder finder = new StationFinder();

    static List<Integer> numbers(List<NearbyStation> result) {
        return result.stream().map(n -> n.station().number()).toList();
    }

    @Nested
    @DisplayName("1.1 Station record")
    class StationRecord {

        @Test
        @DisplayName("record 自动生成访问方法")
        void accessors() {
            assertThat(SMITHFIELD.number()).isEqualTo(42);
            assertThat(SMITHFIELD.availableBikes()).isEqualTo(3);
            assertThat(SMITHFIELD).isEqualTo(new Station(42, "SMITHFIELD NORTH", "Smithfield North", 53.349562, -6.278198, "OPEN", 3, 27, 30));
        }

        @Test
        @DisplayName("isOpen：只有 status 等于 OPEN 才算开放，null 也不算")
        void isOpen() {
            assertThat(SMITHFIELD.isOpen()).isTrue();
            assertThat(BLESSINGTON.isOpen()).isFalse();
            assertThat(new Station(1, "X", null, 0, 0, null, 0, 0, 0).isOpen()).isFalse();
        }

        @Test
        @DisplayName("车或车位数量为负数时抛 IllegalArgumentException")
        void rejectsNegativeCounts() {
            assertThatThrownBy(() -> new Station(1, "X", null, 0, 0, "OPEN", -1, 0, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new Station(1, "X", null, 0, 0, "OPEN", 0, -1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new Station(1, "X", null, 0, 0, "OPEN", 0, 0, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("name 为 null 时抛 NullPointerException")
        void rejectsNullName() {
            assertThatThrownBy(() -> new Station(1, null, null, 0, 0, "OPEN", 0, 0, 0))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("1.2 haversine 距离")
    class Haversine {

        @Test
        @DisplayName("同一个点距离为 0")
        void samePoint() {
            assertThat(GeoUtils.haversineMeters(SPIRE_LAT, SPIRE_LNG, SPIRE_LAT, SPIRE_LNG)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Spire 到 Princes Street 约 87.51 米")
        void shortDistance() {
            assertThat(GeoUtils.haversineMeters(SPIRE_LAT, SPIRE_LNG, 53.349013, -6.260311))
                    .isCloseTo(87.5135, within(0.01));
        }

        @Test
        @DisplayName("都柏林到伦敦约 463 公里")
        void longDistance() {
            assertThat(GeoUtils.haversineMeters(53.3498, -6.2603, 51.5074, -0.1278))
                    .isCloseTo(463_311.06, within(1.0));
        }

        @Test
        @DisplayName("A 到 B 和 B 到 A 距离相同")
        void symmetric() {
            double ab = GeoUtils.haversineMeters(53.3498, -6.2603, 53.330662, -6.260177);
            double ba = GeoUtils.haversineMeters(53.330662, -6.260177, 53.3498, -6.2603);
            assertThat(ab).isCloseTo(ba, within(1e-9));
        }
    }

    @Nested
    @DisplayName("1.3 findNearest：按距离排序并截取前 limit 个")
    class FindNearestBasic {

        @Test
        @DisplayName("Example：limit=3，不加过滤条件")
        void example() {
            List<NearbyStation> result = finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, false, false, 3);
            assertThat(numbers(result)).containsExactly(33, 10, 2);
            assertThat(result).extracting(NearbyStation::distanceMeters).containsExactly(88L, 775L, 933L);
        }

        @Test
        @DisplayName("limit 大于站点数时返回全部站点")
        void limitLargerThanList() {
            List<NearbyStation> result = finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, false, false, 100);
            assertThat(numbers(result)).containsExactly(33, 10, 2, 42, 5);
        }

        @Test
        @DisplayName("limit=0 返回空列表")
        void limitZero() {
            assertThat(finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, false, false, 0)).isEmpty();
        }

        @Test
        @DisplayName("站点列表为空时返回空列表")
        void emptyInput() {
            assertThat(finder.findNearest(SPIRE_LAT, SPIRE_LNG, List.of(), false, false, 3)).isEmpty();
        }

        @Test
        @DisplayName("limit 为负数时抛 IllegalArgumentException")
        void negativeLimit() {
            assertThatThrownBy(() -> finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, false, false, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("1.4 findNearest：过滤条件")
    class FindNearestFilters {

        @Test
        @DisplayName("requireBikes：排除没车的站点和关闭的站点")
        void requireBikes() {
            List<NearbyStation> result = finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, true, false, 10);
            assertThat(numbers(result)).containsExactly(10, 42, 5);
        }

        @Test
        @DisplayName("requireStands：排除没空位的站点和关闭的站点")
        void requireStands() {
            List<NearbyStation> result = finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, false, true, 10);
            assertThat(numbers(result)).containsExactly(33, 42, 5);
        }

        @Test
        @DisplayName("两个条件同时满足")
        void requireBoth() {
            List<NearbyStation> result = finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, true, true, 10);
            assertThat(numbers(result)).containsExactly(42, 5);
        }

        @Test
        @DisplayName("不加过滤条件时，关闭的站点也会返回")
        void closedIncludedWithoutFilters() {
            List<NearbyStation> result = finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, false, false, 10);
            assertThat(numbers(result)).contains(2);
        }
    }

    @Nested
    @DisplayName("1.5 findByNumber：返回 Optional")
    class FindByNumber {

        @Test
        @DisplayName("找到时返回带值的 Optional")
        void found() {
            assertThat(finder.findByNumber(STATIONS, 42)).contains(SMITHFIELD);
        }

        @Test
        @DisplayName("找不到时返回 Optional.empty()，不是 null")
        void notFound() {
            assertThat(finder.findByNumber(STATIONS, 999)).isEmpty();
        }
    }
}
