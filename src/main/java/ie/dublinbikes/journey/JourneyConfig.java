package ie.dublinbikes.journey;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import ie.dublinbikes.journey.google.GoogleMapsApi;


/** Part 14.2: see workbook Part 14 (do 14.1 first) */
// TODO 14.2: this is a configuration class
@EnableConfigurationProperties(GoogleMapsProperties.class)
@Configuration
public class JourneyConfig {

    // TODO 14.2: register as a Bean
    @Bean
    public GoogleMapsApi googleMapsApi(RestClient.Builder builder, GoogleMapsProperties properties) {
        // TODO 14.2: build a RestClient from builder + properties.baseUrl(),
        //            then use HttpServiceProxyFactory to generate an implementation of the GoogleMapsApi interface
        RestClient client = builder.baseUrl(properties.baseUrl()).build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(client))
                .build();

        return factory.createClient(GoogleMapsApi.class);
    }
}
