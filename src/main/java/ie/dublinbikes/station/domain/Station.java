package ie.dublinbikes.station.domain;

/**
 * 一个站点在某一时刻的状态。这是整个项目的核心领域对象，不依赖 Spring、数据库或 JSON。
 *
 * Part 1.1：见练习册 Part 1
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

    // TODO 1.1a: 写一个 compact constructor（紧凑构造函数），校验：
    //   - name 不能是 null（抛 NullPointerException）
    //   - availableBikes、availableStands、totalStands 都不能小于 0（抛 IllegalArgumentException）

    public boolean isOpen() {
        // TODO 1.1b: status 等于 "OPEN" 时返回 true；status 可能是 null
        throw new UnsupportedOperationException("TODO 1.1b");
    }
}
