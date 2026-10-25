package ie.dublinbikes.station.domain;

import java.util.Objects;

/**
 * 一个站点在某一时刻的状态。这是整个项目的核心领域对象，不依赖 Spring、数据库或 JSON。
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

    public Station {
        Objects.requireNonNull(name, "name must not be null");
        if (availableBikes < 0 || availableStands < 0 || totalStands < 0) {
            throw new IllegalArgumentException("bike and stand counts must be >= 0");
        }
    }

    public boolean isOpen() {
        return "OPEN".equals(status);
    }
}
