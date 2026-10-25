package ie.dublinbikes.station.ingest;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ie.dublinbikes.jcdecaux.JcDecauxStationDto;
import ie.dublinbikes.station.persistence.AvailabilityEntity;
import ie.dublinbikes.station.persistence.AvailabilityId;
import ie.dublinbikes.station.persistence.AvailabilityRepository;
import ie.dublinbikes.station.persistence.StationEntity;
import ie.dublinbikes.station.persistence.StationRepository;

@Service
public class StationIngestService {

    private final StationRepository stationRepository;
    private final AvailabilityRepository availabilityRepository;

    public StationIngestService(StationRepository stationRepository,
                                AvailabilityRepository availabilityRepository) {
        this.stationRepository = stationRepository;
        this.availabilityRepository = availabilityRepository;
    }

    @Transactional
    public IngestResult ingest(List<JcDecauxStationDto> dtos) {
        int stationsSaved = 0;
        int availabilityInserted = 0;
        for (JcDecauxStationDto dto : dtos) {
            StationEntity station = stationRepository.findById(dto.number())
                    .orElseGet(() -> new StationEntity(dto.number()));
            station.updateDetails(dto.name(), dto.address(), dto.position().lat(), dto.position().lng(),
                    dto.banking(), dto.bikeStands(), dto.status());
            stationRepository.save(station);
            stationsSaved++;

            Instant lastUpdate = dto.lastUpdateInstant();
            if (lastUpdate == null) {
                continue;
            }
            AvailabilityId id = new AvailabilityId(dto.number(), lastUpdate);
            if (!availabilityRepository.existsById(id)) {
                availabilityRepository.save(new AvailabilityEntity(id, dto.availableBikes(),
                        dto.availableBikeStands(), dto.status()));
                availabilityInserted++;
            }
        }
        return new IngestResult(stationsSaved, availabilityInserted);
    }
}
