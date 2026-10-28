package ie.dublinbikes.jcdecaux;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import ie.dublinbikes.station.domain.Station;

/**
 * JCDecaux /stations 接口返回的一条站点数据，字段和对方的 JSON 一一对应。
 *
 * Part 3.1 / 3.2：见练习册 Part 3
 */
public record JcDecauxStationDto(
        int number,
        // TODO 3.1: JSON 里的字段名是 contract_name、bike_stands、available_bike_stands、
        //           available_bikes、last_update，而 Java 字段是 camelCase。用注解把它们对应起来。
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
        // TODO 3.2a: lastUpdate 是毫秒时间戳，转成 Instant；lastUpdate 为 null 时返回 null
        if(lastUpdate == null){
            return null;
        }else {
            return Instant.ofEpochMilli(lastUpdate);
        }
    }

    public Station toStation() {
        // TODO 3.2b: 转成领域对象 Station（注意字段对应关系）
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
