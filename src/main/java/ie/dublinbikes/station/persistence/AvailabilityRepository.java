package ie.dublinbikes.station.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** Part 4.4 / 4.5：见练习册 Part 4 */
public interface AvailabilityRepository extends JpaRepository<AvailabilityEntity, AvailabilityId> {

    // TODO 4.4a: 声明一个派生查询方法：按主键里的 number 查找，按主键里的 lastUpdate 倒序，只取第一条

    default Optional<AvailabilityEntity> findLatest(int number) {
        // TODO 4.4b: 调用你上面声明的方法
        throw new UnsupportedOperationException("TODO 4.4b");
    }

    // TODO 4.5: 删掉 default 和方法体，改成用 @Query 写一条 JPQL：每个站点只返回 lastUpdate 最大的那一行
    default List<AvailabilityEntity> findLatestForAllStations() {
        throw new UnsupportedOperationException("TODO 4.5");
    }
}
