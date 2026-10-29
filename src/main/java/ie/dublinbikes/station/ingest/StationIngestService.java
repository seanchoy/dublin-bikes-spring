package ie.dublinbikes.station.ingest;

import java.time.Instant;
import java.util.List;

import ie.dublinbikes.jcdecaux.JcDecauxStationDto;
import ie.dublinbikes.station.domain.Station;
import ie.dublinbikes.station.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Part 5.1: see workbook Part 5 */
// TODO 5.1: mark as a Service
@Service
public class StationIngestService {

    private final StationRepository stationRepository;
    private final AvailabilityRepository availabilityRepository;

    public StationIngestService(StationRepository stationRepository,
                                AvailabilityRepository availabilityRepository) {
        this.stationRepository = stationRepository;
        this.availabilityRepository = availabilityRepository;
    }

    // TODO 5.1: run the whole method in one database transaction
    @Transactional
    public IngestResult ingest(List<JcDecauxStationDto> dtos) {
        // TODO 5.1: for each dto:
        //   1. upsert the station: update it if it exists, otherwise create it (StationEntity constructor and updateDetails)
        int stationsSaved = 0;
        int availabilityInserted = 0;
        for(JcDecauxStationDto dto: dtos){
            StationEntity station = stationRepository.findById(dto.number())
                    .orElseGet(() -> new StationEntity(dto.number()));
            station.updateDetails(dto.name(),
                    dto.address(),
                    dto.position().lat(),
                    dto.position().lng(),
                    dto.banking(),
                    dto.bikeStands(),
                    dto.status());
            stationRepository.save(station);
            stationsSaved++;

            //   2. if last_update is not null and (number, last_update) is not in the database yet, insert an AvailabilityEntity
            //   return IngestResult(stations saved, availability rows inserted)

            Instant lastUpdate = dto.lastUpdateInstant();
            if(lastUpdate == null){
                continue;
            }
            AvailabilityId id = new AvailabilityId(dto.number(), lastUpdate);
            if(!availabilityRepository.existsById(id)){
                AvailabilityEntity availability = new AvailabilityEntity(id, dto.availableBikes(), dto.availableBikeStands(), dto.status());
                availabilityRepository.save(availability);
                availabilityInserted++;
            }
        }
        return new IngestResult(stationsSaved, availabilityInserted);

    }
}