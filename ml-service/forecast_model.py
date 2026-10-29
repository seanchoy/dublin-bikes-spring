"""Loads the trained scikit-learn models and runs the two-stage forecast.

Stage 1 predicts weather (temp, humidity, pressure, rain) from hour and month,
unless the caller supplies the weather. Stage 2 predicts available bikes from
station, capacity, hour and weather. Docks are capacity minus bikes, so the two
always add up to the station's capacity.

Ported from main_project/bike_forecast/models/forecast.py in the original
Flask project. The models are the same pickle files; nothing is retrained.
"""
from __future__ import annotations

import json
import os
import pickle
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path

import numpy as np
import pandas as pd

MODEL_DIR = Path(os.getenv("MODEL_DIR", Path(__file__).parent / "models"))


@dataclass(frozen=True)
class Weather:
    avg_temp: float
    avg_humidity: float
    avg_pressure: float
    is_raining: int


@dataclass(frozen=True)
class Prediction:
    time: datetime
    bikes: float
    docks: float
    weather: Weather


class ForecastModel:
    def __init__(self, model_dir: Path = MODEL_DIR):
        def load(name: str):
            with open(model_dir / name, "rb") as fh:
                return pickle.load(fh)

        self.bike_model = load("bike_model.pkl")
        self.temp_model = load("weather_temp.pkl")
        self.humidity_model = load("weather_humidity.pkl")
        self.pressure_model = load("weather_pressure.pkl")
        self.rain_model = load("weather_rain.pkl")
        meta = json.loads((model_dir / "model_meta.json").read_text(encoding="utf-8"))
        self.bike_features: list[str] = meta["bike_features"]
        self.weather_features: list[str] = meta["weather_features"]
        self.humidity_std_mean: float = meta.get("humidity_std_mean", 3.49)

    def predict(self, station_id: int, capacity: int, times: list[datetime],
                weather: list[Weather | None]) -> list[Prediction]:
        if len(times) != len(weather):
            raise ValueError("times and weather must have the same length")
        if not times:
            return []

        resolved = self._resolve_weather(times, weather)
        rows = pd.DataFrame([{
            "station_id": station_id,
            "capacity": capacity,
            "hour": t.hour,
            "avg_temp": w.avg_temp,
            "avg_humidity": w.avg_humidity,
            "avg_pressure": w.avg_pressure,
            "is_raining": w.is_raining,
            "humidity_std": self.humidity_std_mean,
        } for t, w in zip(times, resolved)])[self.bike_features]

        cap = float(capacity)
        raw = np.asarray(self.bike_model.predict(rows), dtype=float).ravel()
        bikes = [round(max(0.0, min(cap, float(b))), 1) for b in raw]
        return [Prediction(time=t, bikes=b, docks=round(cap - b, 1), weather=w)
                for t, b, w in zip(times, bikes, resolved)]

    def _resolve_weather(self, times: list[datetime], weather: list[Weather | None]) -> list[Weather]:
        missing = [i for i, w in enumerate(weather) if w is None]
        if not missing:
            return list(weather)
        frame = pd.DataFrame([{"hour": times[i].hour, "month": times[i].month} for i in missing])[self.weather_features]
        temps = self.temp_model.predict(frame)
        hums = self.humidity_model.predict(frame)
        press = self.pressure_model.predict(frame)
        rains = self.rain_model.predict(frame)
        out = list(weather)
        for k, i in enumerate(missing):
            out[i] = Weather(
                avg_temp=round(float(temps[k]), 1),
                avg_humidity=round(float(hums[k]), 1),
                avg_pressure=round(float(press[k]), 1),
                is_raining=int(float(rains[k])),   # same truncation as the original Flask code
            )
        return out
