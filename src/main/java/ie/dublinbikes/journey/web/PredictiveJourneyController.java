package ie.dublinbikes.journey.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ie.dublinbikes.journey.InvalidDepartureException;
import ie.dublinbikes.journey.PredictiveJourneyService;
import jakarta.validation.Valid;

/**
 * POST /api/v1/journeys/predict: plan a journey for a future departure time.
 *
 * JourneyException (address not found, no station available, Google errors) is handled by
 * JourneyExceptionHandler, so errors have the same format as POST /api/v1/journeys.
 */
@RestController
@RequestMapping("/api/v1/journeys")
public class PredictiveJourneyController {

    private final PredictiveJourneyService journeyService;

    public PredictiveJourneyController(PredictiveJourneyService journeyService) {
        this.journeyService = journeyService;
    }

    @PostMapping("/predict")
    public PredictedJourneyResponse predict(@Valid @RequestBody PredictiveJourneyRequest request) {
        return PredictedJourneyResponse.from(
                journeyService.plan(request.start(), request.end(), request.departAt()));
    }

    /** An @ExceptionHandler declared inside a controller only applies to that controller. */
    @ExceptionHandler(InvalidDepartureException.class)
    public ProblemDetail handleInvalidDeparture(InvalidDepartureException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Invalid departure time");
        return problem;
    }
}
