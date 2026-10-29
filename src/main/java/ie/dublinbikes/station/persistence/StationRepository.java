package ie.dublinbikes.station.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/** Part 4.3: see workbook Part 4 */
public interface StationRepository extends JpaRepository<StationEntity, Integer> {

    // TODO 4.3a: declare a derived query: find by status, ordered by name ascending
    List<StationEntity> findByStatusOrderByNameAsc(String status);

    default List<StationEntity> findOpenStations() {
        // TODO 4.3b: call the method you declared above with status "OPEN"
        return findByStatusOrderByNameAsc("OPEN");
    }
}
