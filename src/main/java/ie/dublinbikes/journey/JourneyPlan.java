package ie.dublinbikes.journey;

import ie.dublinbikes.station.domain.NearbyStation;

/** A complete trip: walk to the pickup station → cycle to the dropoff station → walk to the destination. Provided. */
public record JourneyPlan(LatLng start, LatLng end, NearbyStation pickup, NearbyStation dropoff,
                          RouteLeg walkToPickup, RouteLeg cycle, RouteLeg walkFromDropoff) {

    public long totalDistanceMeters() {
        return walkToPickup.distanceMeters() + cycle.distanceMeters() + walkFromDropoff.distanceMeters();
    }

    public long totalDurationSeconds() {
        return walkToPickup.durationSeconds() + cycle.durationSeconds() + walkFromDropoff.durationSeconds();
    }
}
