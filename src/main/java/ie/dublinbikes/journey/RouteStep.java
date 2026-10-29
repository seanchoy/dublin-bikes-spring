package ie.dublinbikes.journey;

/**
 * One turn-by-turn instruction within a leg, e.g. "Head <b>south</b> on Dawson Street" · "0.2 km" · "1 min".
 * instruction is Google's html_instructions, so it may contain HTML tags. Provided.
 */
public record RouteStep(String instruction, String distanceText, String durationText) {
}
