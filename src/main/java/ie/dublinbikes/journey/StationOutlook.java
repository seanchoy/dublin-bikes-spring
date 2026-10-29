package ie.dublinbikes.journey;

import java.time.LocalDateTime;

import ie.dublinbikes.forecast.HourlyPrediction;
import ie.dublinbikes.station.domain.Station;

/**
 * Bikes and free stands expected at a station when the rider departs (or arrives).
 *
 * When predicted is true, the numbers are the model's prediction for the hour in time.
 * When it is false, the forecast service was unavailable, so these are the current
 * real-time numbers and time is null.
 */
public record StationOutlook(LocalDateTime time, double bikes, double docks, boolean predicted) {

    static StationOutlook predicted(HourlyPrediction p) {
        return new StationOutlook(p.time(), p.bikes(), p.docks(), true);
    }

    static StationOutlook realTime(Station s) {
        return new StationOutlook(null, s.availableBikes(), s.availableStands(), false);
    }
}
