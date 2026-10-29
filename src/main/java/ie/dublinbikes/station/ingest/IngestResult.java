package ie.dublinbikes.station.ingest;

/** Result of one scrape written to the database: how many stations were saved and how many availability rows were inserted. */
public record IngestResult(int stationsSaved, int availabilityInserted) {
}
