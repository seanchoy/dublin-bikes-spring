package ie.dublinbikes.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import ie.dublinbikes.station.domain.StationFinder;

/**
 * Part 2: the Spring container, Beans and configuration binding.
 * ApplicationContextRunner starts a small Spring container with only the given configuration classes, much faster than starting the whole application.
 */
class Part2SpringCoreTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(AppConfig.class);

    @Test
    @DisplayName("2.1 app.jcdecaux.* binds to AppProperties")
    void bindsJcdecauxProperties() {
        runner.withPropertyValues(
                        "app.jcdecaux.api-key=test-key",
                        "app.jcdecaux.contract=lyon",
                        "app.jcdecaux.base-url=http://localhost:9999")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(AppProperties.class);
                    AppProperties.Jcdecaux jc = context.getBean(AppProperties.class).jcdecaux();
                    assertThat(jc.apiKey()).isEqualTo("test-key");
                    assertThat(jc.contract()).isEqualTo("lyon");
                    assertThat(jc.baseUrl()).isEqualTo("http://localhost:9999");
                });
    }

    @Test
    @DisplayName("2.1 app.scraper.* binds, and the string 30s becomes a Duration")
    void bindsScraperProperties() {
        runner.withPropertyValues("app.scraper.enabled=true", "app.scraper.interval=30s")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    AppProperties.Scraper scraper = context.getBean(AppProperties.class).scraper();
                    assertThat(scraper.enabled()).isTrue();
                    assertThat(scraper.interval()).isEqualTo(Duration.ofSeconds(30));
                });
    }

    @Test
    @DisplayName("2.2 Defaults are used when nothing is configured, and nested objects are not null")
    void usesDefaults() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            AppProperties props = context.getBean(AppProperties.class);
            assertThat(props.jcdecaux()).isNotNull();
            assertThat(props.jcdecaux().apiKey()).isNull();
            assertThat(props.jcdecaux().contract()).isEqualTo("dublin");
            assertThat(props.jcdecaux().baseUrl()).isEqualTo("https://api.jcdecaux.com/vls/v1");
            assertThat(props.scraper()).isNotNull();
            assertThat(props.scraper().enabled()).isFalse();
            assertThat(props.scraper().interval()).isEqualTo(Duration.ofMinutes(5));
        });
    }

    @Test
    @DisplayName("2.3 The container has a Clock Bean in the Europe/Dublin time zone")
    void clockBean() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(Clock.class);
            assertThat(context.getBean(Clock.class).getZone()).isEqualTo(ZoneId.of("Europe/Dublin"));
        });
    }

    @Test
    @DisplayName("2.4 Component scanning finds StationFinder")
    void stationFinderIsComponent() {
        new ApplicationContextRunner()
                .withUserConfiguration(ScanDomainPackage.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(StationFinder.class);
                });
    }

    @Configuration
    @ComponentScan(basePackageClasses = StationFinder.class)
    static class ScanDomainPackage {
    }
}
