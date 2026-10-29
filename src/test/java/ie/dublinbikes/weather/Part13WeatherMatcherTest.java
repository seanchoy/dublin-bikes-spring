package ie.dublinbikes.weather;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ie.dublinbikes.forecast.WeatherInput;

/** Part 13.3: matching 3-hourly forecasts to each hour (pure logic; read the tests before writing the code). */
class Part13WeatherMatcherTest {

    static final ZoneId DUBLIN = ZoneId.of("Europe/Dublin");

    static WeatherSnapshot w(String time, double temp, int humidity, int pressure, double rainMm, double pop) {
        return new WeatherSnapshot(Instant.parse(time), temp, temp - 2, humidity, pressure, 4.0, "", rainMm, pop);
    }

    // On 28 October Dublin is on winter time, so local time = UTC
    static final List<WeatherSnapshot> FORECAST = List.of(
            w("2026-10-28T18:00:00Z", 9.0, 88, 1013, 0.0, 0.1),
            w("2026-10-28T21:00:00Z", 8.0, 85, 1014, 0.9, 0.7),
            w("2026-10-29T00:00:00Z", 7.0, 83, 1015, 0.0, 0.0));

    static List<LocalDateTime> times(String... t) {
        return java.util.Arrays.stream(t).map(LocalDateTime::parse).toList();
    }

    @Test
    @DisplayName("Example: each hour takes the closest forecast slot")
    void picksNearest() {
        List<WeatherInput> result = WeatherMatcher.match(
                times("2026-10-28T18:00", "2026-10-28T20:00", "2026-10-28T23:00"), FORECAST, DUBLIN);

        assertThat(result).containsExactly(
                new WeatherInput(9.0, 88, 1013, 0),
                new WeatherInput(8.0, 85, 1014, 1),
                new WeatherInput(7.0, 83, 1015, 0));
    }

    @Test
    @DisplayName("Equally close to the slots before and after: takes the earlier one")
    void tieGoesToEarlier() {
        List<WeatherInput> result = WeatherMatcher.match(
                times("2026-10-28T19:30"), FORECAST, DUBLIN);

        assertThat(result).containsExactly(new WeatherInput(9.0, 88, 1013, 0));
    }

    @Test
    @DisplayName("Rain: rainfall > 0.01 mm, or probability of rain >= 0.5")
    void rainRule() {
        List<WeatherSnapshot> rainy = List.of(
                w("2026-10-28T18:00:00Z", 9.0, 80, 1010, 0.02, 0.0),
                w("2026-10-28T21:00:00Z", 9.0, 80, 1010, 0.0, 0.5),
                w("2026-10-29T00:00:00Z", 9.0, 80, 1010, 0.01, 0.49));

        List<WeatherInput> result = WeatherMatcher.match(
                times("2026-10-28T18:00", "2026-10-28T21:00", "2026-10-29T00:00"), rainy, DUBLIN);

        assertThat(result).extracting(WeatherInput::isRaining).containsExactly(1, 1, 0);
    }

    @Test
    @DisplayName("No forecasts at all: returns null (the Python service infers the weather itself)")
    void emptyForecast() {
        assertThat(WeatherMatcher.match(times("2026-10-28T18:00"), List.of(), DUBLIN)).isNull();
    }

    @Test
    @DisplayName("Summer time: in July 15:00 in Dublin is 14:00 UTC, so convert the time zone before comparing")
    void convertsLocalTimeWithZone() {
        List<WeatherSnapshot> summer = List.of(
                w("2026-07-01T12:00:00Z", 18.0, 70, 1020, 0.0, 0.0),
                w("2026-07-01T15:00:00Z", 21.0, 60, 1019, 0.0, 0.0));

        // local 15:00 = 14:00 UTC, closer to 15:00Z (1 hour vs 2 hours)
        assertThat(WeatherMatcher.match(times("2026-07-01T15:00"), summer, DUBLIN))
                .containsExactly(new WeatherInput(21.0, 60, 1019, 0));
        // local 14:00 = 13:00 UTC, closer to 12:00Z
        assertThat(WeatherMatcher.match(times("2026-07-01T14:00"), summer, DUBLIN))
                .containsExactly(new WeatherInput(18.0, 70, 1020, 0));
    }
}
