package ie.dublinbikes.station.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/** Part 4.3：见练习册 Part 4 */
public interface StationRepository extends JpaRepository<StationEntity, Integer> {

    // TODO 4.3a: 声明一个派生查询方法：按 status 查找，结果按 name 升序排列
    List<StationEntity> findByStatusOrderByNameAsc(String status);

    default List<StationEntity> findOpenStations() {
        // TODO 4.3b: 调用你上面声明的方法，status 传 "OPEN"
        return findByStatusOrderByNameAsc("OPEN");
    }
}
