package ie.dublinbikes.station.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** availability 表的一行：某个站点在某个时刻有几辆车、几个空位。 */
@Entity
@Table(name = "availability")
public class AvailabilityEntity {

    @EmbeddedId
    private AvailabilityId id;

    @Column(name = "available_bikes")
    private int availableBikes;

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
