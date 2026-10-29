package ie.dublinbikes.ops;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Part 10.2 / 10.3: API docs and Actuator.
 * Starts the whole application (like FinalApplicationTest), so Parts 7–9 must be done first.
 * The inference service URL points at a port where nothing is listening, so its health is always DOWN.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.forecast.base-url=http://localhost:1")
class Part10ApiDocsTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("10.2 /v3/api-docs: title, version and the description of the forecast endpoint")
    void openApiDocs() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Dublin Bikes API"))
                .andExpect(jsonPath("$.info.version").value("v1"))
                .andExpect(jsonPath("$.paths['/api/v1/stations/{number}/forecast'].get.summary")
                        .value("Forecast bikes for the next hours"))
                .andExpect(jsonPath("$.paths['/api/v1/stations/{number}/forecast'].get.tags[0]").value("Forecast"))
                .andExpect(jsonPath("$.paths['/api/v1/stations'].get").exists());
    }

    @Test
    @DisplayName("10.2 The Swagger page opens")
    void swaggerUi() throws Exception {
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("10.3 /actuator/health shows each component: the inference service is unreachable, so overall DOWN (503)")
    void healthShowsComponents() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.components.db.status").value("UP"))
                .andExpect(jsonPath("$.components.forecastService.status").value("DOWN"))
                .andExpect(jsonPath("$.components.forecastService.details.circuitBreaker").value("CLOSED"));
    }
}
