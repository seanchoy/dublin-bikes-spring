package ie.dublinbikes.journey.web;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for POST /api/v1/journeys/predict. departAt is Dublin local time, e.g. "2026-10-30T08:00".
 */
public record PredictiveJourneyRequest(@NotBlank String start, @NotBlank String end, @NotNull LocalDateTime departAt) {
}
