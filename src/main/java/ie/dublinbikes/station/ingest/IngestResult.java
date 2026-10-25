package ie.dublinbikes.station.ingest;

/** 一次抓取写库的结果：更新了几个站点、新插入了几条可用量记录。 */
public record IngestResult(int stationsSaved, int availabilityInserted) {
}
