package ie.dublinbikes.journey.web;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /api/v1/journeys.
 *
 * Part 15.3: see workbook Part 15
 */
// TODO 15.3: start and end must not be empty (null, "" and whitespace-only are all rejected)
public record JourneyRequest(@NotBlank String start, @NotBlank String end) {
}
