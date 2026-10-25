package ie.dublinbikes.station.web;

import ie.dublinbikes.station.domain.NearbyStation;

public record NearbyStationResponse(
        int number,
        String name,
        double lat,
        double lng,
        int availableBikes,
        int availableStands,
        long distanceMeters) {

    public static NearbyStationResponse from(NearbyStation nearby) {
        var s = nearby.station();
        return new NearbyStationResponse(s.number(), s.name(), s.lat(), s.lng(),
                s.availableBikes(), s.availableStands(), nearby.distanceMeters());
    }
}
