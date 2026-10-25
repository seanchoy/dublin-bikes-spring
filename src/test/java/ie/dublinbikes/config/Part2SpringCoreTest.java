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
 * Part 2：Spring 容器、Bean、配置绑定。
 * ApplicationContextRunner 会启动一个只包含指定配置类的小型 Spring 容器，比启动整个应用快得多。
 */
class Part2SpringCoreTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(AppConfig.class);

    @Test
    @DisplayName("2.1 app.jcdecaux.* 配置绑定到 AppProperties")
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
    @DisplayName("2.1 app.scraper.* 配置绑定，字符串 30s 自动转成 Duration")
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
    @DisplayName("2.2 什么都不配置时使用默认值，而且嵌套对象不是 null")
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
    @DisplayName("2.3 容器里有一个 Clock Bean，时区是 Europe/Dublin")
    void clockBean() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(Clock.class);
            assertThat(context.getBean(Clock.class).getZone()).isEqualTo(ZoneId.of("Europe/Dublin"));
        });
    }

    @Test
    @DisplayName("2.4 组件扫描能找到 StationFinder")
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
