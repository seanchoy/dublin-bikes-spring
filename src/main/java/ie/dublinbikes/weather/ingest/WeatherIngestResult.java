package ie.dublinbikes.weather.ingest;

/** Result of one weather scrape written to the database. Provided. */
public record WeatherIngestResult(boolean currentInserted, int forecastSaved) {
}
