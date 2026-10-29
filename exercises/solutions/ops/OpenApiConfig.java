package ie.dublinbikes.ops;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dublinBikesOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Dublin Bikes API")
                .version("v1")
                .description("Live station availability and ML forecasts for Dublin Bikes."));
    }
}
