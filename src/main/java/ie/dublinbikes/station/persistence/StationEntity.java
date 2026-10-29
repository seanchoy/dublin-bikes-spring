package ie.dublinbikes.station.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One row of the station table: static station information (name, location, total stands).
 *
 * Part 4.1: see workbook Part 4. The table is defined in src/main/resources/db/migration/V1__create_station_tables.sql
 */
// TODO 4.1: mark this class as a JPA entity mapped to the station table
@Entity
@Table(name = "station")
public class StationEntity {

    // TODO 4.1: number is the primary key
    @Id
    private int number;

    // TODO 4.1: the name column must not be null
    @Column(nullable = false)
    private String name;

    private String address;

    private double lat;

    private double lng;

    private boolean banking;

    // TODO 4.1: name the bike_stands column explicitly (Spring Boot already maps camelCase to snake_case; this is just practice)
    @Column(name = "bike_stands")
    private int bikeStands;

    private String status;

    /** JPA needs a no-arg constructor to create objects through reflection; protected keeps business code from using it by mistake. */
    protected StationEntity() {
    }

    public StationEntity(int number) {
        this.number = number;
    }

    public void updateDetails(String name, String address, double lat, double lng,
                              boolean banking, int bikeStands, String status) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.banking = banking;
        this.bikeStands = bikeStands;
        this.status = status;
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public boolean isBanking() {
        return banking;
    }

    public int getBikeStands() {
        return bikeStands;
    }

    public String getStatus() {
        return status;
    }
}
