"""FastAPI wrapper around the forecast models. Internal service: the Spring Boot
backend is its only client, and it is not exposed to browsers."""
from __future__ import annotations

from contextlib import asynccontextmanager
from datetime import datetime

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from forecast_model import ForecastModel, Weather

model: ForecastModel | None = None


@asynccontextmanager
async def lifespan(_: FastAPI):
    global model
    model = ForecastModel()          # ~2 GB of trees; loaded once at startup
    yield


app = FastAPI(title="Dublin Bikes forecast service", lifespan=lifespan)


class WeatherIn(BaseModel):
    avg_temp: float
    avg_humidity: float
    avg_pressure: float
    is_raining: int = Field(ge=0, le=1)


class PredictRequest(BaseModel):
    station_id: int = Field(ge=0)
    capacity: int = Field(gt=0)
    times: list[datetime] = Field(min_length=1, max_length=48)
    weather: list[WeatherIn | None] | None = None


class PredictionOut(BaseModel):
    time: datetime
    bikes: float
    docks: float
    avg_temp: float
    avg_humidity: float
    avg_pressure: float
    is_raining: int


class PredictResponse(BaseModel):
    station_id: int
    predictions: list[PredictionOut]


@app.get("/health")
def health() -> dict:
    return {"status": "UP" if model is not None else "DOWN"}


@app.post("/predict", response_model=PredictResponse)
def predict(req: PredictRequest) -> PredictResponse:
    if model is None:
        raise HTTPException(status_code=503, detail="models not loaded")
    weather = req.weather or [None] * len(req.times)
    if len(weather) != len(req.times):
        raise HTTPException(status_code=422, detail="weather must have one entry per time")
    preds = model.predict(
        req.station_id, req.capacity, req.times,
        [None if w is None else Weather(**w.model_dump()) for w in weather],
    )
    return PredictResponse(station_id=req.station_id, predictions=[
        PredictionOut(time=p.time, bikes=p.bikes, docks=p.docks,
                      avg_temp=p.weather.avg_temp, avg_humidity=p.weather.avg_humidity,
                      avg_pressure=p.weather.avg_pressure, is_raining=p.weather.is_raining)
        for p in preds
    ])
