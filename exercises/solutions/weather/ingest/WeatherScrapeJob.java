package ie.dublinbikes.weather.ingest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ie.dublinbikes.config.AppProperties;
import ie.dublinbikes.weather.WeatherSnapshot;
import ie.dublinbikes.weather.WeatherUnavailableException;
import ie.dublinbikes.weather.openweather.OpenWeatherClient;

@Component
public class WeatherScrapeJob {

    private static final Logger log = LoggerFactory.getLogger(WeatherScrapeJob.class);

    private final OpenWeatherClient client;
    private final WeatherIngestService ingestService;
    private final AppProperties properties;

    public WeatherScrapeJob(OpenWeatherClient client, WeatherIngestService ingestService, AppProperties properties) {
        this.client = client;
        this.ingestService = ingestService;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${app.weather.interval:10m}")
    public void run() {
        if (!properties.scraper().enabled()) {
            return;
        }
        try {
            WeatherSnapshot current = client.current();
            WeatherIngestResult result = ingestService.ingest(current, client.forecast());
            log.info("Scraped weather: current inserted={}, forecast rows={}",
                    result.currentInserted(), result.forecastSaved());
        } catch (WeatherUnavailableException e) {
            log.warn("Weather scrape failed: {}", e.getMessage());
        }
    }
}
