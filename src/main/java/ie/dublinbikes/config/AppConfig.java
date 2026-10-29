package ie.dublinbikes.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.ZoneId;


/**
 * Application-wide configuration.
 *
 * Part 2.1 / 2.3: see workbook Part 2
 * Part 5.3: see workbook Part 5
 */
// TODO 2.1: mark this class as a configuration class and enable binding for AppProperties
// TODO 5.3: enable scheduled tasks
@Configuration
@EnableConfigurationProperties(AppProperties.class)
@EnableScheduling
public class AppConfig {

    // TODO 2.3: register the return value of this method as a Bean
    @Bean
    public Clock clock() {
        // TODO 2.3: return a system clock using the Europe/Dublin time zone
        return Clock.system(ZoneId.of("Europe/Dublin"));
    }

}
