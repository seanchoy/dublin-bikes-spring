package ie.dublinbikes.station.ingest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ie.dublinbikes.config.AppProperties;
import ie.dublinbikes.jcdecaux.JcDecauxClient;

/** Part 5.2 / 5.3：见练习册 Part 5 */
// TODO 5.2: 让 Spring 管理这个类
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

    // TODO 5.3: 让这个方法每隔 app.scraper.interval 执行一次（上一次执行完之后再开始计时）
    public void run() {
        // TODO 5.2: 开关关闭就直接返回；否则抓取 → 写库 → 打 info 日志；
        //           JcDecauxException 要捕获并打 warn 日志，不能往外抛
        throw new UnsupportedOperationException("TODO 5.2");
    }
}
