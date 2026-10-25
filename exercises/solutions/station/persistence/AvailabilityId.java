package ie.dublinbikes.station.persistence;

import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** availability 表的复合主键：(站点编号, 更新时间)。 */
@Embeddable
public record AvailabilityId(
        int number,
        @Column(name = "last_update") Instant lastUpdate) implements Serializable {
}
