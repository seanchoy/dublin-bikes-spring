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

    // TODO 4.5: remove default and the method body, and write a JPQL @Query instead: only the row with the latest lastUpdate per station
    @Query("""
            select a from AvailabilityEntity a 
            where a.id.lastUpdate = (
                        select max(a2.id.lastUpdate) from AvailabilityEntity a2
                        where a.id.number = a2.id.number
                        )            
            """)
    List<AvailabilityEntity> findLatestForAllStations();
}
