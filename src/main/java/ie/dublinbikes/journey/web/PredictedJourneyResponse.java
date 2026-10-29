package ie.dublinbikes.journey.web;

import java.time.LocalDateTime;

import ie.dublinbikes.journey.PredictedJourneyPlan;
import ie.dublinbikes.journey.StationOutlook;

/**
 * Predicted journey returned to the frontend. journey has the same shape as the POST /api/v1/journeys response;
 * the outlooks say how many bikes / stands each station is expected to have, and whether a prediction was used.
 */
public record PredictedJourneyResponse(LocalDateTime departAt, JourneyResponse journey,
                                       StationOutlook pickupOutlook, StationOutlook dropoffOutlook) {

    public static PredictedJourneyResponse from(PredictedJourneyPlan p) {
        return new PredictedJourneyResponse(p.departAt(), JourneyResponse.from(p.journey()),
                p.pickupOutlook(), p.dropoffOutlook());
    }
}
