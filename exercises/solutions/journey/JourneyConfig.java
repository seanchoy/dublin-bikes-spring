package ie.dublinbikes.journey;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import ie.dublinbikes.journey.google.GoogleMapsApi;

@Configuration
@EnableConfigurationProperties(GoogleMapsProperties.class)
public class JourneyConfig {

    @Bean
    public GoogleMapsApi googleMapsApi(RestClient.Builder builder, GoogleMapsProperties properties) {
        RestClient restClient = builder.baseUrl(properties.baseUrl()).build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(GoogleMapsApi.class);
    }
}
