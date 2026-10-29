package ie.dublinbikes.journey.web;

import jakarta.validation.constraints.NotBlank;

/** POST /api/v1/journeys 的请求体。 */
public record JourneyRequest(@NotBlank String start, @NotBlank String end) {
}
