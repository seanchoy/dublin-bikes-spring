package ie.dublinbikes.station.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Part 1: modern Java (record, Stream, Optional). No Spring in this part. */
class Part1ModernJavaTest {

    // Query point: the Spire on O'Connell Street
    static final double SPIRE_LAT = 53.3498;
    static final double SPIRE_LNG = -6.2603;

    static final Station PRINCES = new Station(33, "PRINCES STREET", "Princes Street", 53.349013, -6.260311, "OPEN", 0, 20, 20);
    static final Station DAME = new Station(10, "DAME STREET", "Dame Street", 53.344007, -6.266802, "OPEN", 5, 0, 16);
    static final Station BLESSINGTON = new Station(2, "BLESSINGTON STREET", "Blessington Street", 53.356769, -6.26814, "CLOSED", 7, 13, 20);
    static final Station SMITHFIELD = new Station(42, "SMITHFIELD NORTH", "Smithfield North", 53.349562, -6.278198, "OPEN", 3, 27, 30);
    static final Station CHARLEMONT = new Station(5, "CHARLEMONT PLACE", "Charlemont Place", 53.330662, -6.260177, "OPEN", 10, 30, 40);

    // deliberately out of order
    static final List<Station> STATIONS = List.of(SMITHFIELD, CHARLEMONT, PRINCES, BLESSINGTON, DAME);

    final StationFinder finder = new StationFinder();

    static List<Integer> numbers(List<NearbyStation> result) {
        return result.stream().map(n -> n.station().number()).toList();
    }

    @Nested
    @DisplayName("1.1 Station record")
    class StationRecord {

        @Test
        @DisplayName("record generates accessor methods")
        void accessors() {
            assertThat(SMITHFIELD.number()).isEqualTo(42);
            assertThat(SMITHFIELD.availableBikes()).isEqualTo(3);
            assertThat(SMITHFIELD).isEqualTo(new Station(42, "SMITHFIELD NORTH", "Smithfield North", 53.349562, -6.278198, "OPEN", 3, 27, 30));
        }

        @Test
        @DisplayName("isOpen: only status OPEN counts as open; null does not")
        void isOpen() {
            assertThat(SMITHFIELD.isOpen()).isTrue();
            assertThat(BLESSINGTON.isOpen()).isFalse();
            assertThat(new Station(1, "X", null, 0, 0, null, 0, 0, 0).isOpen()).isFalse();
        }

        @Test
        @DisplayName("Negative bike or stand counts throw IllegalArgumentException")
        void rejectsNegativeCounts() {
            assertThatThrownBy(() -> new Station(1, "X", null, 0, 0, "OPEN", -1, 0, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new Station(1, "X", null, 0, 0, "OPEN", 0, -1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new Station(1, "X", null, 0, 0, "OPEN", 0, 0, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("A null name throws NullPointerException")
        void rejectsNullName() {
            assertThatThrownBy(() -> new Station(1, null, null, 0, 0, "OPEN", 0, 0, 0))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("1.2 haversine distance")
    class Haversine {

        @Test
        @DisplayName("The same point is 0 apart")
        void samePoint() {
            assertThat(GeoUtils.haversineMeters(SPIRE_LAT, SPIRE_LNG, SPIRE_LAT, SPIRE_LNG)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Spire to Princes Street is about 87.51 m")
        void shortDistance() {
            assertThat(GeoUtils.haversineMeters(SPIRE_LAT, SPIRE_LNG, 53.349013, -6.260311))
                    .isCloseTo(87.5135, within(0.01));
        }

        @Test
        @DisplayName("Dublin to London is about 463 km")
        void longDistance() {
            assertThat(GeoUtils.haversineMeters(53.3498, -6.2603, 51.5074, -0.1278))
                    .isCloseTo(463_311.06, within(1.0));
        }

        @Test
        @DisplayName("A to B is the same distance as B to A")
        void symmetric() {
            double ab = GeoUtils.haversineMeters(53.3498, -6.2603, 53.330662, -6.260177);
            double ba = GeoUtils.haversineMeters(53.330662, -6.260177, 53.3498, -6.2603);
            assertThat(ab).isCloseTo(ba, within(1e-9));
        }
    }

    @Nested
    @DisplayName("1.3 findNearest: sort by distance and keep the first limit")
    class FindNearestBasic {

        @Test
        @DisplayName("Example: limit=3, no filters")
        void example() {
            List<NearbyStation> result = finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, false, false, 3);
            assertThat(numbers(result)).containsExactly(33, 10, 2);
            assertThat(result).extracting(NearbyStation::distanceMeters).containsExactly(88L, 775L, 933L);
        }

        @Test
        @DisplayName("A limit larger than the number of stations returns every station")
        void limitLargerThanList() {
            List<NearbyStation> result = finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, false, false, 100);
            assertThat(numbers(result)).containsExactly(33, 10, 2, 42, 5);
        }

        @Test
        @DisplayName("limit=0 returns an empty list")
        void limitZero() {
            assertThat(finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, false, false, 0)).isEmpty();
        }

        @Test
        @DisplayName("An empty station list returns an empty list")
        void emptyInput() {
            assertThat(finder.findNearest(SPIRE_LAT, SPIRE_LNG, List.of(), false, false, 3)).isEmpty();
        }

        @Test
        @DisplayName("A negative limit throws IllegalArgumentException")
        void negativeLimit() {
            assertThatThrownBy(() -> finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, false, false, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("1.4 findNearest: filters")
    class FindNearestFilters {

        @Test
        @DisplayName("requireBikes: excludes stations without bikes and closed stations")
        void requireBikes() {
            List<NearbyStation> result = finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, true, false, 10);
            assertThat(numbers(result)).containsExactly(10, 42, 5);
        }

        @Test
        @DisplayName("requireStands: excludes stations without free stands and closed stations")
        void requireStands() {
            List<NearbyStation> result = finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, false, true, 10);
            assertThat(numbers(result)).containsExactly(33, 42, 5);
        }

        @Test
        @DisplayName("Both filters at once")
        void requireBoth() {
            List<NearbyStation> result = finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, true, true, 10);
            assertThat(numbers(result)).containsExactly(42, 5);
        }

        @Test
        @DisplayName("Without filters, closed stations are returned too")
        void closedIncludedWithoutFilters() {
            List<NearbyStation> result = finder.findNearest(SPIRE_LAT, SPIRE_LNG, STATIONS, false, false, 10);
            assertThat(numbers(result)).contains(2);
        }
    }

    @Nested
    @DisplayName("1.5 findByNumber: returns an Optional")
    class FindByNumber {

        @Test
        @DisplayName("Returns a non-empty Optional when found")
        void found() {
            assertThat(finder.findByNumber(STATIONS, 42)).contains(SMITHFIELD);
        }

        @Test
        @DisplayName("Returns Optional.empty(), not null, when not found")
        void notFound() {
            assertThat(finder.findByNumber(STATIONS, 999)).isEmpty();
        }
    }
}
