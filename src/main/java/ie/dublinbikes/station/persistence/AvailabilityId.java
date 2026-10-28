package ie.dublinbikes.station.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.Instant;

/**
 * availability 表的复合主键：(站点编号, 更新时间)。
 *
 * Part 4.2：见练习册 Part 4
 */
// TODO 4.2: 标记成"可以嵌入到实体里的值对象"
@Embeddable
public record AvailabilityId(
        int number,
        // TODO 4.2: 对应 last_update 列
        @Column(name = "last_update")
        Instant lastUpdate) implements Serializable {
}
