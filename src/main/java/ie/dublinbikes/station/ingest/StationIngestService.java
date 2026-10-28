package ie.dublinbikes.station.ingest;

import java.time.Instant;
import java.util.List;

import ie.dublinbikes.jcdecaux.JcDecauxStationDto;
import ie.dublinbikes.station.domain.Station;
import ie.dublinbikes.station.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Part 5.1：见练习册 Part 5 */
// TODO 5.1: 标记成 Service
@Service
public class StationIngestService {

    private final StationRepository stationRepository;
    private final AvailabilityRepository availabilityRepository;

    public StationIngestService(StationRepository stationRepository,
                                AvailabilityRepository availabilityRepository) {
        this.stationRepository = stationRepository;
        this.availabilityRepository = availabilityRepository;
    }

    // TODO 5.1: 整个方法放在一个数据库事务里
    @Transactional
    public IngestResult ingest(List<JcDecauxStationDto> dtos) {
        // TODO 5.1: 对每个 dto：
        //   1. 站点 upsert：数据库里有就更新，没有就新建（StationEntity 的构造函数和 updateDetails）
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

            //   2. last_update 不为 null 且 (number, last_update) 在数据库里还不存在时，插入一条 AvailabilityEntity
            //   返回 IngestResult(保存了几个站点, 新插入了几条可用量)

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