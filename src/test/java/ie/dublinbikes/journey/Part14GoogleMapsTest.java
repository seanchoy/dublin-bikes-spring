package ie.dublinbikes.journey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.web.util.UriComponentsBuilder;

import ie.dublinbikes.journey.JourneyException.Reason;
import ie.dublinbikes.journey.google.GoogleMapsApi;
import ie.dublinbikes.journey.google.GoogleMapsService;

/**
 * Part 14: calling Google Maps with an @HttpExchange declarative client.
 * GoogleMapsApi is only an interface; the @Bean in JourneyConfig generates its implementation with HttpServiceProxyFactory.
 * That implementation uses the injected RestClient.Builder underneath, so MockRestServiceServer can still intercept it.
 */
@RestClientTest(GoogleMapsService.class)
@Import(JourneyConfig.class)
@TestPropertySource(properties = {
        "app.google.server-key=server-key",
        "app.google.base-url=https://maps.test/api"
})
class Part14GoogleMapsTest {

    @Autowired
    GoogleMapsService maps;

    @Autowired
    GoogleMapsApi api;

    @Autowired
    MockRestServiceServer server;

    /** Checks a query parameter (URL-decoded before comparing, so write "53.3498,-6.2603" rather than "53.3498%2C-6.2603"). */
    static RequestMatcher param(String name, String expected) {
        return request -> {
            String raw = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst(name);
            String decoded = raw == null ? null : URLDecoder.decode(raw, StandardCharsets.UTF_8);
            assertThat(decoded).as("query param " + name).isEqualTo(expected);
        };
    }

    static ClassPathResource fixture(String name) {
        return new ClassPathResource("fixtures/" + name);
    }

    static String json(String status) {
        return "{\"status\":\"" + status + "\",\"results\":[],\"routes\":[]}";
    }

    @Test
    @DisplayName("14.1 GoogleMapsApi is an interface, and the Spring container has an implementation of it (a proxy generated at runtime)")
    void apiIsAProxy() {
        assertThat(GoogleMapsApi.class.isInterface()).isTrue();
        assertThat(api).isNotNull();
        assertThat(api.getClass().getName()).doesNotContain("GoogleMapsApiImpl");
    }

    @Test
    @DisplayName("14.2 geocode: sends address (with Dublin, Ireland appended) and the server key, returns the coordinates")
    void geocode() {
        server.expect(requestTo(Matchers.startsWith("https://maps.test/api/geocode/json")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(param("address", "O'Connell Street, Dublin, Ireland"))
                .andExpect(param("key", "server-key"))
                .andRespond(withSuccess(fixture("google-geocode.json"), MediaType.APPLICATION_JSON));

        assertThat(maps.geocode("O'Connell Street")).isEqualTo(new LatLng(53.3509, -6.2604));
    }

    @Test
    @DisplayName("14.2 directions: origin / destination are \"lat,lng\", mode is bicycling, returns one leg")
    void directions() {
        server.expect(requestTo(Matchers.startsWith("https://maps.test/api/directions/json")))
                .andExpect(param("origin", "53.3498,-6.2603"))
                .andExpect(param("destination", "53.3381,-6.2591"))
                .andExpect(param("mode", "bicycling"))
                .andExpect(param("key", "server-key"))
                .andRespond(withSuccess(fixture("google-directions.json"), MediaType.APPLICATION_JSON));

        RouteLeg leg = maps.directions(new LatLng(53.3498, -6.2603), new LatLng(53.3381, -6.2591), TravelMode.BICYCLING);

        assertThat(leg).isEqualTo(new RouteLeg(TravelMode.BICYCLING, 1634, 372, "1.6 km", "6 mins", "a~l~Fjk~uOwHJy@P",
                List.of(new RouteStep("Head <b>south</b>", "0.2 km", null))));    // the fixture's step has no duration
    }

    @Test
    @DisplayName("14.3 Address not found (ZERO_RESULTS): JourneyException with reason ADDRESS_NOT_FOUND")
    void addressNotFound() {
        server.expect(requestTo(Matchers.startsWith("https://maps.test/api/geocode/json")))
                .andRespond(withSuccess(json("ZERO_RESULTS"), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> maps.geocode("Nowhere Street"))
                .isInstanceOfSatisfying(JourneyException.class,
                        e -> assertThat(e.getReason()).isEqualTo(Reason.ADDRESS_NOT_FOUND));
    }

    @Test
    @DisplayName("14.3 New requirement: a Google-side problem such as REQUEST_DENIED is not \"address not found\" but ROUTE_UNAVAILABLE")
    void requestDeniedIsUpstreamError() {
        server.expect(requestTo(Matchers.startsWith("https://maps.test/api/geocode/json")))
                .andRespond(withSuccess(json("REQUEST_DENIED"), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> maps.geocode("O'Connell Street"))
                .isInstanceOfSatisfying(JourneyException.class, e -> {
                    assertThat(e.getReason()).isEqualTo(Reason.ROUTE_UNAVAILABLE);
                    assertThat(e).hasMessageContaining("REQUEST_DENIED");
                });
    }

    @Test
    @DisplayName("14.3 No route found: ROUTE_UNAVAILABLE")
    void noRoute() {
        server.expect(requestTo(Matchers.startsWith("https://maps.test/api/directions/json")))
                .andRespond(withSuccess(json("ZERO_RESULTS"), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> maps.directions(new LatLng(53.3, -6.2), new LatLng(51.5, -0.1), TravelMode.WALKING))
                .isInstanceOfSatisfying(JourneyException.class,
                        e -> assertThat(e.getReason()).isEqualTo(Reason.ROUTE_UNAVAILABLE));
    }

    @Test
    @DisplayName("14.3 Google returns 500: also becomes JourneyException (ROUTE_UNAVAILABLE), so RestClient exceptions do not leak out")
    void serverError() {
        server.expect(requestTo(Matchers.startsWith("https://maps.test/api/geocode/json"))).andRespond(withServerError());

        assertThatThrownBy(() -> maps.geocode("O'Connell Street"))
                .isInstanceOfSatisfying(JourneyException.class,
                        e -> assertThat(e.getReason()).isEqualTo(Reason.ROUTE_UNAVAILABLE));
    }
}
