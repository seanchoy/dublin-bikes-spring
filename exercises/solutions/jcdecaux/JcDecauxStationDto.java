package ie.dublinbikes.jcdecaux;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import ie.dublinbikes.station.domain.Station;

/** JCDecaux /stations 接口返回的一条站点数据，字段和对方的 JSON 一一对应。 */
public record JcDecauxStationDto(
        int number,
        @JsonProperty("contract_name") String contractName,
        String name,
        String address,
        Position position,
        boolean banking,
        boolean bonus,
        @JsonProperty("bike_stands") int bikeStands,
        @JsonProperty("available_bike_stands") int availableBikeStands,
        @JsonProperty("available_bikes") int availableBikes,
        String status,
        @JsonProperty("last_update") Long lastUpdate) {

    public record Position(double lat, double lng) {
    }

    public Instant lastUpdateInstant() {
        return lastUpdate == null ? null : Instant.ofEpochMilli(lastUpdate);
    }

    public Station toStation() {
        return new Station(number, name, address, position.lat(), position.lng(), status,
                availableBikes, availableBikeStands, bikeStands);
    }
}
