package ie.dublinbikes.journey.web;

import ie.dublinbikes.journey.JourneyPlan;
import ie.dublinbikes.journey.LatLng;
import ie.dublinbikes.journey.RouteLeg;
import ie.dublinbikes.station.web.NearbyStationResponse;

/** Route returned to the frontend. Provided. */
public record JourneyResponse(LatLng start, LatLng end,
                              NearbyStationResponse pickup, NearbyStationResponse dropoff,
                              RouteLeg walkToPickup, RouteLeg cycle, RouteLeg walkFromDropoff,
                              long totalDistanceMeters, long totalDurationSeconds) {

    public static JourneyResponse from(JourneyPlan p) {
        return new JourneyResponse(p.start(), p.end(),
                NearbyStationResponse.from(p.pickup()), NearbyStationResponse.from(p.dropoff()),
                p.walkToPickup(), p.cycle(), p.walkFromDropoff(),
                p.totalDistanceMeters(), p.totalDurationSeconds());
    }
}
