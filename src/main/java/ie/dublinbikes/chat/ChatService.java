package ie.dublinbikes.chat;

import java.util.ArrayList;
import java.util.List;

import ie.dublinbikes.station.StationQueryService;
import ie.dublinbikes.station.domain.Station;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/** Part 17.1 / 17.2: see workbook Part 17 */
// TODO 17.2: mark as a Service
@Service
public class ChatService {

    /** Base instructions for the system prompt. Provided. */
    static final String BASE_PROMPT = """
            You are a helpful assistant for Dublin Bikes. \
            Help users find stations, plan cycling routes, check weather, and give tips. \
            Keep replies short and friendly. \
            When answering about bike availability, use ONLY the live station data provided below - \
            never guess or make up numbers.""";
    static final int MAX_STATIONS = 50;

    private final GroqClient groqClient;
    private final StationQueryService stationQueryService;
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    public ChatService(GroqClient groqClient, StationQueryService stationQueryService) {
        this.groqClient = groqClient;
        this.stationQueryService = stationQueryService;
    }

    public static String buildSystemPrompt(List<Station> stations) {
        // TODO 17.1: BASE_PROMPT; when there are stations, append "\n\nLIVE DUBLIN BIKES DATA (as of right now):",
        //            then one line per station "\n- name: X bikes, Y spaces (status)", at most MAX_STATIONS
        StringBuilder prompt = new StringBuilder(BASE_PROMPT);
        int num = 0;
        if(!stations.isEmpty()){
            prompt.append("\n\nLIVE DUBLIN BIKES DATA (as of right now):");
            for(Station s: stations){

                String name = s.name();
                int availableBikes = s.availableBikes();
                int availableStands = s.availableStands();
                String status = String.valueOf(s.status());
                prompt.append(String.format("\n- %s: %d bikes, %d spaces (%s)", name, availableBikes, availableStands, status));
                num += 1;
                if(num == MAX_STATIONS){
                    break;
                }
            }
        }
        return prompt.toString();
    }

    public String reply(List<ChatMessage> history) {
        // TODO 17.2: clean up the history (drop empty messages, trim whitespace, treat any role other than user as assistant);
        //            throw IllegalArgumentException if there is no user message;
        //            load stations (treat errors as no stations), put the system prompt first, pass it to groqClient

        List<ChatMessage> validMessages = new ArrayList<>();

        for(ChatMessage cm: history){
            if( cm == null || cm.content() == null || cm.content().isBlank()){
                continue;
            }

            String role = ("user".equals(cm.role())) ? "user" : "assistant";
            validMessages.add(new ChatMessage(role, cm.content().strip()));
        }

        if(validMessages.stream().noneMatch(m -> "user".equals(m.role()))) {
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
        messages.addAll(validMessages);

        return groqClient.complete(messages);

    }
}
