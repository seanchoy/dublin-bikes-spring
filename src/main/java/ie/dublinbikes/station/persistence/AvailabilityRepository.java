package ie.dublinbikes.station.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** Part 4.4 / 4.5: see workbook Part 4 */
public interface AvailabilityRepository extends JpaRepository<AvailabilityEntity, AvailabilityId> {

     // TODO 4.4a: declare a derived query: find by the number in the key, order by the lastUpdate in the key descending, first result only
    Optional<AvailabilityEntity> findFirstByIdNumberOrderByIdLastUpdateDesc(int number);

    default Optional<AvailabilityEntity> findLatest(int number) {
        // TODO 4.4b: call the method you declared above
        return findFirstByIdNumberOrderByIdLastUpdateDesc(number);
    }

    // Group first, then join back on the primary key. The grouping uses a skip scan on (number, last_update),
    // so the cost follows the station count. A correlated subquery here would re-evaluate MAX for every history row.
    @Query(value = """
            SELECT a.number, a.last_update, a.available_bikes, a.available_bike_stands, a.status
            FROM availability a
            JOIN (
                SELECT number, MAX(last_update) AS last_update
                FROM availability
                GROUP BY number
            ) latest ON a.number = latest.number AND a.last_update = latest.last_update
            """, nativeQuery = true)
    List<AvailabilityEntity> findLatestForAllStations();
}
