package ie.dublinbikes.journey;

import java.time.LocalDateTime;

/**
 * A normal journey plus what the pickup and dropoff stations are expected to look like at that time.
 */
public record PredictedJourneyPlan(JourneyPlan journey, LocalDateTime departAt,
                                   StationOutlook pickupOutlook, StationOutlook dropoffOutlook) {
}
