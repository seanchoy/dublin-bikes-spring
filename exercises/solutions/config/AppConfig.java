package ie.dublinbikes.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
@EnableScheduling
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Europe/Dublin"));
    }
}
