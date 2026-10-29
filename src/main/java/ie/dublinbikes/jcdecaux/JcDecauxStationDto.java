package ie.dublinbikes.jcdecaux;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import ie.dublinbikes.station.domain.Station;

/**
 * One station returned by the JCDecaux /stations endpoint; fields match their JSON one to one.
 *
 * Part 3.1 / 3.2: see workbook Part 3
 */
public record JcDecauxStationDto(
        int number,
        // TODO 3.1: the JSON fields are contract_name, bike_stands, available_bike_stands,
        //           available_bikes and last_update, but the Java fields are camelCase. Map them with annotations.
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
        // TODO 3.2a: lastUpdate is a timestamp in milliseconds; convert it to an Instant, or return null if lastUpdate is null
        if(lastUpdate == null){
            return null;
        }else {
            return Instant.ofEpochMilli(lastUpdate);
        }
    }

    public Station toStation() {
        // TODO 3.2b: convert to the Station domain object (watch how the fields map)
        return new Station(
                number,
                name,
                address,
                position.lat,
                position.lng,
                status,
                availableBikes,
                availableBikeStands,
                availableBikes+availableBikeStands);
    }
}
