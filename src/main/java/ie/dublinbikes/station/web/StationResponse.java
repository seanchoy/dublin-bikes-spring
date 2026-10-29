package ie.dublinbikes.station.web;

import ie.dublinbikes.station.domain.Station;

/** JSON returned to the frontend by GET /api/v1/stations. */
public record StationResponse(
        int number,
        String name,
        String address,
        double lat,
        double lng,
        String status,
        int availableBikes,
        int availableStands,
        int totalStands) {

    public static StationResponse from(Station s) {
        return new StationResponse(s.number(), s.name(), s.address(), s.lat(), s.lng(), s.status(),
                s.availableBikes(), s.availableStands(), s.totalStands());
    }
}
