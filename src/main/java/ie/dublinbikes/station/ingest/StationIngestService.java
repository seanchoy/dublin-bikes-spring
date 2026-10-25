package ie.dublinbikes.station.ingest;

import java.util.List;

import ie.dublinbikes.jcdecaux.JcDecauxStationDto;
import ie.dublinbikes.station.persistence.AvailabilityRepository;
import ie.dublinbikes.station.persistence.StationRepository;

/** Part 5.1：见练习册 Part 5 */
// TODO 5.1: 标记成 Service
public class StationIngestService {

    private final StationRepository stationRepository;
    private final AvailabilityRepository availabilityRepository;

    public StationIngestService(StationRepository stationRepository,
                                AvailabilityRepository availabilityRepository) {
        this.stationRepository = stationRepository;
        this.availabilityRepository = availabilityRepository;
    }

    // TODO 5.1: 整个方法放在一个数据库事务里
    public IngestResult ingest(List<JcDecauxStationDto> dtos) {
        // TODO 5.1: 对每个 dto：
        //   1. 站点 upsert：数据库里有就更新，没有就新建（StationEntity 的构造函数和 updateDetails）
        //   2. last_update 不为 null 且 (number, last_update) 在数据库里还不存在时，插入一条 AvailabilityEntity
        //   返回 IngestResult(保存了几个站点, 新插入了几条可用量)
        throw new UnsupportedOperationException("TODO 5.1");
    }
}
