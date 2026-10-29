package ie.dublinbikes.weather.ingest;

import ie.dublinbikes.weather.WeatherSnapshot;
import ie.dublinbikes.weather.WeatherUnavailableException;
import ie.dublinbikes.weather.openweather.OpenWeatherDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ie.dublinbikes.config.AppProperties;
import ie.dublinbikes.weather.openweather.OpenWeatherClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/** Part 12.3: see workbook Part 12 */
// TODO 12.3: let Spring manage this class
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

    // TODO 12.3: run every app.weather.interval (default 10m)
    @Scheduled(fixedDelayString = "${app.weather.interval:10m}")
    public void run() {
        // TODO 12.3: same as StationScrapeJob in Part 5: return if the switch is off;
        //            otherwise fetch the current weather and forecast and pass them to ingestService; only log a warning for WeatherUnavailableException

        if(!properties.scraper().enabled()){
            return;
        }

        try{
            WeatherSnapshot current = client.current();
            List<WeatherSnapshot> forecast = client.forecast();
            WeatherIngestResult ingestResult = ingestService.ingest(current, forecast);
            log.info("Scraped weather: current inserted={}, forecast saved={}",
                    ingestResult.currentInserted(), ingestResult.forecastSaved());
        }catch (WeatherUnavailableException e){
            log.warn("OpenWeather scrape failed: {}", e.getMessage());
        }
    }
}
