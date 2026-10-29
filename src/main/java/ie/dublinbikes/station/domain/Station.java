package ie.dublinbikes.station.domain;

import java.util.Objects;

/**
 * The state of one station at one moment. This is the core domain object of the project and does not depend on Spring, the database or JSON.
 *
 * Part 1.1: see workbook Part 1
 */
public record Station(
        int number,
        String name,
        String address,
        double lat,
        double lng,
        String status,
        int availableBikes,
        int availableStands,
        int totalStands) {

    // TODO 1.1a: write a compact constructor that checks:
    //   - name is not null (throw NullPointerException)
    //   - availableBikes, availableStands and totalStands are not negative (throw IllegalArgumentException)
    public Station {
        Objects.requireNonNull(name, "name can not be null");
        if(availableBikes < 0 || availableStands < 0 || totalStands <0){
            throw new IllegalArgumentException("availableBikes, availableStands and totalStands must be greater than 0");
        }

    }

    public boolean isOpen() {
        // TODO 1.1b: return true when status equals "OPEN"; status may be null
        return "OPEN".equals(status);
    }
}
