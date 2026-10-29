package ie.dublinbikes.weather.persistence;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import ie.dublinbikes.weather.WeatherSnapshot;

/** One row of the weather_current table: current weather at one moment (insert only, never updated). */
@Entity
@Table(name = "weather_current")
public class CurrentWeatherEntity {

    @Id
    private Instant observedAt;

    private double temperature;
    private double feelsLike;
    private int humidity;
    private int pressure;
    private double windSpeed;
    private String description;
    private double rainMm;

    protected CurrentWeatherEntity() {
    }

    public static CurrentWeatherEntity from(WeatherSnapshot w) {
        CurrentWeatherEntity e = new CurrentWeatherEntity();
        e.observedAt = w.time();
        e.temperature = w.temperature();
        e.feelsLike = w.feelsLike();
        e.humidity = w.humidity();
        e.pressure = w.pressure();
        e.windSpeed = w.windSpeed();
        e.description = w.description();
        e.rainMm = w.rainMm();
        return e;
    }

    public WeatherSnapshot toSnapshot() {
        return new WeatherSnapshot(observedAt, temperature, feelsLike, humidity, pressure, windSpeed,
                description, rainMm, 0.0);
    }

    public Instant getObservedAt() {
        return observedAt;
    }
}
