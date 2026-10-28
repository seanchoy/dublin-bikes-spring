package ie.dublinbikes.station.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * availability 表的一行：某个站点在某个时刻有几辆车、几个空位。
 *
 * Part 4.2：见练习册 Part 4
 */
// TODO 4.2: 标记成 JPA 实体，对应 availability 表
@Entity
@Table(name = "availability")
public class AvailabilityEntity {

    // TODO 4.2: 这个字段是复合主键
    @EmbeddedId
    private AvailabilityId id;

    // TODO 4.2: 对应 available_bikes 列
    @Column(name = "available_bikes")
    private int availableBikes;

    // TODO 4.2: 对应 available_bike_stands 列
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
