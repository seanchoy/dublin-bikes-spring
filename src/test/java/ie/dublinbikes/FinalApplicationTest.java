package ie.dublinbikes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import ie.dublinbikes.forecast.StationForecastController;
import ie.dublinbikes.station.ingest.StationScrapeJob;
import ie.dublinbikes.station.web.StationController;

/**
 * The final check: start the whole application. It only passes once all the earlier parts are done.
 * It catches problems that isolated tests cannot, such as a Bean missing a dependency or an entity that does not match its table.
 */
@SpringBootTest
class FinalApplicationTest {

    @Autowired
    ApplicationContext context;

    @Test
    @DisplayName("The whole application starts and the key Beans are in the container")
    void contextLoads() {
        assertThat(context.getBean(StationController.class)).isNotNull();
        assertThat(context.getBean(StationScrapeJob.class)).isNotNull();
        assertThat(context.getBean(StationForecastController.class)).isNotNull();   // Part 9
    }
}
