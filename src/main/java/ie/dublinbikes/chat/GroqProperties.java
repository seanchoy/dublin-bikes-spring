package ie.dublinbikes.chat;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Configuration under app.groq.*. Provided. */
@ConfigurationProperties(prefix = "app.groq")
public record GroqProperties(
        String apiKey,
        @DefaultValue("openai/gpt-oss-120b") String model,
        @DefaultValue("https://api.groq.com/openai/v1") String baseUrl,
        @DefaultValue("800") int maxTokens,
        @DefaultValue("0.7") double temperature) {
}
