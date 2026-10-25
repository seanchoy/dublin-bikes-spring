package ie.dublinbikes.station.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AvailabilityRepository extends JpaRepository<AvailabilityEntity, AvailabilityId> {

    Optional<AvailabilityEntity> findFirstByIdNumberOrderByIdLastUpdateDesc(int number);

    default Optional<AvailabilityEntity> findLatest(int number) {
        return findFirstByIdNumberOrderByIdLastUpdateDesc(number);
    }

    @Query("""
            select a from AvailabilityEntity a
            where a.id.lastUpdate = (
                select max(b.id.lastUpdate) from AvailabilityEntity b
                where b.id.number = a.id.number)
            """)
    List<AvailabilityEntity> findLatestForAllStations();
}
