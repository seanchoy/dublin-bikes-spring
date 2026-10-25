package ie.dublinbikes.station.ingest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ie.dublinbikes.config.AppProperties;
import ie.dublinbikes.jcdecaux.JcDecauxClient;
import ie.dublinbikes.jcdecaux.JcDecauxException;
import ie.dublinbikes.jcdecaux.JcDecauxStationDto;

@Component
public class StationScrapeJob {

    private static final Logger log = LoggerFactory.getLogger(StationScrapeJob.class);

    private final JcDecauxClient client;
    private final StationIngestService ingestService;
    private final AppProperties properties;

    public StationScrapeJob(JcDecauxClient client, StationIngestService ingestService, AppProperties properties) {
        this.client = client;
        this.ingestService = ingestService;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${app.scraper.interval:5m}")
    public void run() {
        if (!properties.scraper().enabled()) {
            return;
        }
        try {
            List<JcDecauxStationDto> dtos = client.fetchStations();
            IngestResult result = ingestService.ingest(dtos);
            log.info("Scraped {} stations, inserted {} availability rows",
                    result.stationsSaved(), result.availabilityInserted());
        } catch (JcDecauxException e) {
            log.warn("Station scrape failed: {}", e.getMessage());
        }
    }
}
