package ie.dublinbikes.journey;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;

import ie.dublinbikes.forecast.HourlyPrediction;
import ie.dublinbikes.forecast.StationForecast;
import ie.dublinbikes.forecast.StationForecastService;
import ie.dublinbikes.journey.JourneyException.Reason;
import ie.dublinbikes.journey.google.GoogleMapsService;
import ie.dublinbikes.station.StationQueryService;
import ie.dublinbikes.station.domain.GeoUtils;
import ie.dublinbikes.station.domain.NearbyStation;
import ie.dublinbikes.station.domain.Station;
import ie.dublinbikes.station.domain.StationFinder;

/**
 * Plans a journey by choosing stations from the forecast for the departure time.
 *
 * Unlike JourneyService, the pickup station must be expected to have bikes when the rider
 * leaves, and the dropoff station must be expected to have free stands when they arrive,
 * rather than having them right now.
 */
@Service
public class PredictiveJourneyService {

    /** How many candidate stations to consider near the start and near the end. */
    static final int CANDIDATES = 5;

    /** The station forecast covers at most 48 hours. */
    static final int MAX_HOURS_AHEAD = 48;

    /** A station counts as having bikes / stands if at least this many are expected. */
    static final double MIN_AVAILABLE = 1.0;

    /** Average speed used to estimate cycling time, about 14 km/h. */
    static final double CYCLING_METERS_PER_SECOND = 4.0;

    private final GoogleMapsService maps;
    private final StationQueryService stationQueryService;
    private final StationFinder stationFinder;
    private final StationForecastService forecastService;
    private final Clock clock;

    public PredictiveJourneyService(GoogleMapsService maps, StationQueryService stationQueryService,
                                    StationFinder stationFinder, StationForecastService forecastService, Clock clock) {
        this.maps = maps;
        this.stationQueryService = stationQueryService;
        this.stationFinder = stationFinder;
        this.forecastService = forecastService;
        this.clock = clock;
    }

    /** departAt is Dublin local time, the same kind of time as the hourly forecast. */
    public PredictedJourneyPlan plan(String startAddress, String endAddress, LocalDateTime departAt) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (departAt.isBefore(now) || departAt.isAfter(now.plusHours(MAX_HOURS_AHEAD))) {
            throw new InvalidDepartureException("departAt must be between now and " + MAX_HOURS_AHEAD + " hours from now");
        }

        LatLng start = maps.geocode(startAddress);
        LatLng end = maps.geocode(endAddress);
        List<Station> openStations = stationQueryService.listStations().stream()
                .filter(Station::isOpen)
                .toList();

        Choice pickup = choose(start, openStations, departAt, now, o -> o.bikes() >= MIN_AVAILABLE,
                "No station is expected to have bikes near the start at " + departAt);
        LatLng pickupAt = locationOf(pickup.nearby().station());
        RouteLeg walkToPickup = maps.directions(start, pickupAt, TravelMode.WALKING);

        // The dropoff needs free stands on arrival: departure + walking time + cycling time estimated from straight-line distance
        LocalDateTime arrival = departAt
                .plusSeconds(walkToPickup.durationSeconds())
                .plusSeconds(estimatedCyclingSeconds(pickupAt, end));
        Choice dropoff = choose(end, openStations, arrival, now, o -> o.docks() >= MIN_AVAILABLE,
                "No station is expected to have free stands near the destination at " + arrival);
        LatLng dropoffAt = locationOf(dropoff.nearby().station());

        JourneyPlan journey = new JourneyPlan(start, end, pickup.nearby(), dropoff.nearby(),
                walkToPickup,
                maps.directions(pickupAt, dropoffAt, TravelMode.BICYCLING),
                maps.directions(dropoffAt, end, TravelMode.WALKING));
        return new PredictedJourneyPlan(journey, departAt, pickup.outlook(), dropoff.outlook());
    }

    /**
     * Checks candidates from nearest to farthest and picks the first one that qualifies.
     * Each check calls the forecast service, so it stops at the first match instead of forecasting all of them.
     */
    private Choice choose(LatLng point, List<Station> stations, LocalDateTime at, LocalDateTime now,
                          Predicate<StationOutlook> good, String message) {
        int hours = hoursToCover(now, at);
        List<NearbyStation> candidates = stationFinder.findNearest(point.lat(), point.lng(), stations,
                false, false, CANDIDATES);
        for (NearbyStation candidate : candidates) {
            StationOutlook outlook = outlookAt(candidate.station(), at, hours);
            if (good.test(outlook)) {
                return new Choice(candidate, outlook);
            }
        }
        throw new JourneyException(Reason.NO_STATION_AVAILABLE, message);
    }

    /** Uses the forecast hour closest to at; falls back to real-time numbers if the forecast is unavailable. */
    private StationOutlook outlookAt(Station station, LocalDateTime at, int hours) {
        StationForecast forecast = forecastService.forecast(station.number(), hours);
        Optional<HourlyPrediction> slot = forecast.predictions().stream()
                .min(Comparator.comparing((HourlyPrediction p) -> Duration.between(p.time(), at).abs())
                        .thenComparing(HourlyPrediction::time));
        if (forecast.degraded() || slot.isEmpty()) {
            return StationOutlook.realTime(station);
        }
        return StationOutlook.predicted(slot.get());
    }

    /**
     * The forecast starts at the next full hour, one point per hour. Returns how many hours to request so it reaches at.
     * Example: now 17:20, at 19:40 → 2 hours from 17:00 → request 3 points (18:00, 19:00, 20:00).
     */
    static int hoursToCover(LocalDateTime now, LocalDateTime at) {
        long hours = Duration.between(now.truncatedTo(ChronoUnit.HOURS), at).toHours() + 1;
        return (int) Math.clamp(hours, 1, MAX_HOURS_AHEAD);
    }

    private static long estimatedCyclingSeconds(LatLng from, LatLng to) {
        double meters = GeoUtils.haversineMeters(from.lat(), from.lng(), to.lat(), to.lng());
        return Math.round(meters / CYCLING_METERS_PER_SECOND);
    }

    private static LatLng locationOf(Station station) {
        return new LatLng(station.lat(), station.lng());
    }

    private record Choice(NearbyStation nearby, StationOutlook outlook) {
    }
}
