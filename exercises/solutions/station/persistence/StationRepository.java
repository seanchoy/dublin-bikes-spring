package ie.dublinbikes.station.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<StationEntity, Integer> {

    List<StationEntity> findByStatusOrderByNameAsc(String status);

    default List<StationEntity> findOpenStations() {
        return findByStatusOrderByNameAsc("OPEN");
    }
}
