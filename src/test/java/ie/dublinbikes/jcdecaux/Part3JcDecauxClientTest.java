package ie.dublinbikes.jcdecaux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import ie.dublinbikes.config.AppConfig;
import ie.dublinbikes.station.domain.Station;

/**
 * Part 3：用 RestClient 调用外部 API，用 Jackson 把 JSON 转成 record。
 * MockRestServiceServer 会拦截 RestClient 发出的请求，返回我们准备好的假数据，不会真的访问 JCDecaux。
 */
@RestClientTest(JcDecauxClient.class)
@Import(AppConfig.class)
@TestPropertySource(properties = {
        "app.jcdecaux.api-key=test-key",
        "app.jcdecaux.base-url=https://jcdecaux.test/vls/v1"
})
class Part3JcDecauxClientTest {

    static final String EXPECTED_URL = "https://jcdecaux.test/vls/v1/stations?contract=dublin&apiKey=test-key";

    @Autowired
    JcDecauxClient client;

    @Autowired
    MockRestServiceServer server;

    @Test
    @DisplayName("3.1 JSON 的 snake_case 字段正确映射到 record")
    void mapsJsonFields() {
        server.expect(requestTo(EXPECTED_URL))
                .andRespond(withSuccess(new ClassPathResource("fixtures/jcdecaux-stations.json"), MediaType.APPLICATION_JSON));

        List<JcDecauxStationDto> dtos = client.fetchStations();

        assertThat(dtos).hasSize(3);
        JcDecauxStationDto smithfield = dtos.get(0);
        assertThat(smithfield.number()).isEqualTo(42);
        assertThat(smithfield.contractName()).isEqualTo("dublin");
        assertThat(smithfield.name()).isEqualTo("SMITHFIELD NORTH");
        assertThat(smithfield.position().lat()).isEqualTo(53.349562);
        assertThat(smithfield.position().lng()).isEqualTo(-6.278198);
        assertThat(smithfield.bikeStands()).isEqualTo(30);
        assertThat(smithfield.availableBikeStands()).isEqualTo(27);
        assertThat(smithfield.availableBikes()).isEqualTo(3);
        assertThat(smithfield.lastUpdate()).isEqualTo(1761400800000L);
        assertThat(dtos.get(1).banking()).isTrue();
        assertThat(dtos.get(2).lastUpdate()).isNull();
    }

    @Test
    @DisplayName("3.2 DTO 转成领域对象 Station，last_update 转成 Instant")
    void convertsToStation() {
        var dto = new JcDecauxStationDto(42, "dublin", "SMITHFIELD NORTH", "Smithfield North",
                new JcDecauxStationDto.Position(53.349562, -6.278198), false, false, 30, 27, 3, "OPEN", 1761400800000L);

        Station station = dto.toStation();

        assertThat(station).isEqualTo(new Station(42, "SMITHFIELD NORTH", "Smithfield North",
                53.349562, -6.278198, "OPEN", 3, 27, 30));
        assertThat(dto.lastUpdateInstant()).isEqualTo(Instant.parse("2025-10-25T14:00:00Z"));

        var noTimestamp = new JcDecauxStationDto(1, "dublin", "X", "X",
                new JcDecauxStationDto.Position(0, 0), false, false, 0, 0, 0, "OPEN", null);
        assertThat(noTimestamp.lastUpdateInstant()).isNull();
    }

    @Test
    @DisplayName("3.3 请求的 URL、HTTP 方法和查询参数正确")
    void sendsCorrectRequest() {
        server.expect(requestTo(EXPECTED_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThat(client.fetchStations()).isEmpty();
        server.verify();
    }

    @Test
    @DisplayName("3.4 对方返回 4xx/5xx 时抛 JcDecauxException，消息里带状态码")
    void httpErrorBecomesJcDecauxException() {
        server.expect(requestTo(EXPECTED_URL)).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> client.fetchStations())
                .isInstanceOf(JcDecauxException.class)
                .hasMessageContaining("403");
    }

    @Test
    @DisplayName("3.4 网络错误时也抛 JcDecauxException，并保留原始异常作为 cause")
    void networkErrorBecomesJcDecauxException() {
        server.expect(requestTo(EXPECTED_URL)).andRespond(withException(new IOException("connection reset")));

        assertThatThrownBy(() -> client.fetchStations())
                .isInstanceOf(JcDecauxException.class)
                .hasCauseInstanceOf(Exception.class);
    }
}
