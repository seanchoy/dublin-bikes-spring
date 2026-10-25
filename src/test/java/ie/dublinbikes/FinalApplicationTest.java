package ie.dublinbikes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import ie.dublinbikes.station.ingest.StationScrapeJob;
import ie.dublinbikes.station.web.StationController;

/**
 * 最后一关：启动整个应用。前面 6 关全部完成后这个测试才会通过。
 * 它能发现单独测试时发现不了的问题，比如某个 Bean 缺少依赖、实体和表结构对不上。
 */
@SpringBootTest
class FinalApplicationTest {

    @Autowired
    ApplicationContext context;

    @Test
    @DisplayName("整个应用能启动，关键 Bean 都在容器里")
    void contextLoads() {
        assertThat(context.getBean(StationController.class)).isNotNull();
        assertThat(context.getBean(StationScrapeJob.class)).isNotNull();
    }
}
