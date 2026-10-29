package ie.dublinbikes.station.ingest;

import ie.dublinbikes.jcdecaux.JcDecauxException;
import ie.dublinbikes.jcdecaux.JcDecauxStationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ie.dublinbikes.config.AppProperties;
import ie.dublinbikes.jcdecaux.JcDecauxClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/** Part 5.2 / 5.3: see workbook Part 5 */
// TODO 5.2: let Spring manage this class
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

    // TODO 5.3: run this method every app.scraper.interval (counting from when the previous run finished)
    @Scheduled(fixedDelayString = "${app.scraper.interval:5m}")
    public void run() {
        // TODO 5.2: return straight away if the switch is off; otherwise fetch → save → log at info;
        //           catch JcDecauxException and log a warning instead of rethrowing
        if(!properties.scraper().enabled()){
            return;
        }

        try{

            List<JcDecauxStationDto> dtos = client.fetchStations();
            IngestResult result = ingestService.ingest(dtos);
            log.info("Scraped {} stations, inserted {} availability rows",
                    result.stationsSaved(), result.availabilityInserted());

        } catch (JcDecauxException e) {
            log.warn("Station scrape failed: {}", e.getMessage());
        }


    }
}
