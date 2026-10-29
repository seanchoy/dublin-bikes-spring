package ie.dublinbikes.station.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * One row of the availability table: how many bikes and free stands a station had at one moment.
 *
 * Part 4.2: see workbook Part 4
 */
// TODO 4.2: mark as a JPA entity mapped to the availability table
@Entity
@Table(name = "availability")
public class AvailabilityEntity {

    // TODO 4.2: this field is a composite primary key
    @EmbeddedId
    private AvailabilityId id;

    // TODO 4.2: maps to the available_bikes column
    @Column(name = "available_bikes")
    private int availableBikes;

    // TODO 4.2: maps to the available_bike_stands column
    @Column(name = "available_bike_stands")
    private int availableBikeStands;

    private String status;

    protected AvailabilityEntity() {
    }

    public AvailabilityEntity(AvailabilityId id, int availableBikes, int availableBikeStands, String status) {
        this.id = id;
        this.availableBikes = availableBikes;
        this.availableBikeStands = availableBikeStands;
        this.status = status;
    }

    public AvailabilityId getId() {
        return id;
    }

    public int getAvailableBikes() {
        return availableBikes;
    }

    public int getAvailableBikeStands() {
        return availableBikeStands;
    }

    public String getStatus() {
        return status;
    }
}
