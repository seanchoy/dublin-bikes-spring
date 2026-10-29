package ie.dublinbikes.ops;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Part 10.2a: see workbook Part 10 */
// TODO 10.2a: this is a configuration class
@Configuration
public class OpenApiConfig {

    // TODO 10.2a: register as a Bean
    @Bean
    public OpenAPI dublinBikesOpenApi() {
        // TODO 10.2a: title "Dublin Bikes API", version "v1"
        return new OpenAPI().info(new Info().title("Dublin Bikes API").version("v1"));
    }
}
