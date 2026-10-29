package ie.dublinbikes.chat;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ie.dublinbikes.station.StationQueryService;
import ie.dublinbikes.station.domain.Station;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    static final String BASE_PROMPT = """
            You are a helpful assistant for Dublin Bikes. \
            Help users find stations, plan cycling routes, check weather, and give tips. \
            Keep replies short and friendly. \
            When answering about bike availability, use ONLY the live station data provided below - \
            never guess or make up numbers.""";
    static final int MAX_STATIONS = 50;

    private final GroqClient groqClient;
    private final StationQueryService stationQueryService;

    public ChatService(GroqClient groqClient, StationQueryService stationQueryService) {
        this.groqClient = groqClient;
        this.stationQueryService = stationQueryService;
    }

    public static String buildSystemPrompt(List<Station> stations) {
        if (stations.isEmpty()) {
            return BASE_PROMPT;
        }
        StringBuilder sb = new StringBuilder(BASE_PROMPT).append("\n\nLIVE DUBLIN BIKES DATA (as of right now):");
        stations.stream().limit(MAX_STATIONS).forEach(s -> sb.append("\n- ")
                .append(s.name()).append(": ")
                .append(s.availableBikes()).append(" bikes, ")
                .append(s.availableStands()).append(" spaces (")
                .append(s.status()).append(")"));
        return sb.toString();
    }

    public String reply(List<ChatMessage> history) {
        List<ChatMessage> conversation = new ArrayList<>();
        for (ChatMessage m : history) {
            if (m == null || m.content() == null || m.content().isBlank()) {
                continue;
            }
            String role = "user".equals(m.role()) ? "user" : "assistant";
            conversation.add(new ChatMessage(role, m.content().strip()));
        }
        if (conversation.stream().noneMatch(m -> "user".equals(m.role()))) {
            throw new IllegalArgumentException("At least one non-empty user message is required");
        }

        List<Station> stations;
        try {
            stations = stationQueryService.listStations();
        } catch (RuntimeException e) {
            log.warn("Chat without station data: {}", e.getMessage());
            stations = List.of();
        }

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.system(buildSystemPrompt(stations)));
        messages.addAll(conversation);
        return groqClient.complete(messages);
    }
}
