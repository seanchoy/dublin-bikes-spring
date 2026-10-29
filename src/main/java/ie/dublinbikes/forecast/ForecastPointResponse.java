package ie.dublinbikes.forecast;

import java.time.LocalDateTime;

/** One hour of forecast returned to the frontend. Fields are camelCase (our own API style). Provided. */
public record ForecastPointResponse(LocalDateTime time, double bikes, double docks,
                                    double temperature, double humidity, double pressure, boolean raining) {

    public static ForecastPointResponse from(HourlyPrediction p) {
        return new ForecastPointResponse(p.time(), p.bikes(), p.docks(),
                p.avgTemp(), p.avgHumidity(), p.avgPressure(), p.isRaining() == 1);
    }
}
