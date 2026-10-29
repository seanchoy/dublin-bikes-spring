package ie.dublinbikes.station.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.Instant;

/**
 * Composite primary key of the availability table: (station number, update time).
 *
 * Part 4.2: see workbook Part 4
 */
// TODO 4.2: mark as a value object that can be embedded in an entity
@Embeddable
public record AvailabilityId(
        int number,
        // TODO 4.2: maps to the last_update column
        @Column(name = "last_update")
        Instant lastUpdate) implements Serializable {
}
