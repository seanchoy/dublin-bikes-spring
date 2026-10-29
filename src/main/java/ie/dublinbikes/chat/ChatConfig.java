package ie.dublinbikes.chat;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Enables binding for GroqProperties. Provided. */
@Configuration
@EnableConfigurationProperties(GroqProperties.class)
public class ChatConfig {
}
