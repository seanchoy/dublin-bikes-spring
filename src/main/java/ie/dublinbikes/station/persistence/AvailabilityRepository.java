package ie.dublinbikes.station.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Part 4.4 / 4.5：见练习册 Part 4 */
public interface AvailabilityRepository extends JpaRepository<AvailabilityEntity, AvailabilityId> {

     // TODO 4.4a: 声明一个派生查询方法：按主键里的 number 查找，按主键里的 lastUpdate 倒序，只取第一条
    Optional<AvailabilityEntity> findFirstByIdNumberOrderByIdLastUpdateDesc(int number);

    default Optional<AvailabilityEntity> findLatest(int number) {
        // TODO 4.4b: 调用你上面声明的方法
        return findFirstByIdNumberOrderByIdLastUpdateDesc(number);
    }

    // TODO 4.5: 删掉 default 和方法体，改成用 @Query 写一条 JPQL：每个站点只返回 lastUpdate 最大的那一行
    @Query("""
            select a from AvailabilityEntity a 
            where a.id.lastUpdate = (
                        select max(a2.id.lastUpdate) from AvailabilityEntity a2
                        where a.id.number = a2.id.number
                        )            
            """)
    List<AvailabilityEntity> findLatestForAllStations();
}
