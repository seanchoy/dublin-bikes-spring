package ie.dublinbikes.weather.persistence;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import ie.dublinbikes.weather.WeatherSnapshot;

/** One row of the weather_forecast table: the weather for one forecast time. Overwritten with the latest forecast on each scrape. */
@Entity
@Table(name = "weather_forecast")
public class ForecastWeatherEntity {

    @Id
    private Instant forecastTime;

    private double temperature;
    private double feelsLike;
    private int humidity;
    private int pressure;
    private double windSpeed;
    private String description;
    private double rainMm;
    private double rainProbability;
    private Instant fetchedAt;

    protected ForecastWeatherEntity() {
    }

    public ForecastWeatherEntity(Instant forecastTime) {
        this.forecastTime = forecastTime;
    }

    public void update(WeatherSnapshot w, Instant fetchedAt) {
        this.temperature = w.temperature();
        this.feelsLike = w.feelsLike();
        this.humidity = w.humidity();
        this.pressure = w.pressure();
        this.windSpeed = w.windSpeed();
        this.description = w.description();
        this.rainMm = w.rainMm();
        this.rainProbability = w.rainProbability();
        this.fetchedAt = fetchedAt;
    }

    public WeatherSnapshot toSnapshot() {
        return new WeatherSnapshot(forecastTime, temperature, feelsLike, humidity, pressure, windSpeed,
                description, rainMm, rainProbability);
    }

    public Instant getForecastTime() {
        return forecastTime;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }
}
