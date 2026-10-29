/*
 * API compatibility layer.
 *
 * The page scripts (main.js, stations.js, MLbikes.js, journey.js) were written for the original
 * Flask API. The Spring Boot backend serves a redesigned API under /api/v1 with camelCase JSON.
 * This file wraps window.fetch: requests to an old endpoint are sent to the new one, and the new
 * response is converted back to the shape the old scripts expect. It must load before any other
 * script on the page.
 */
(function () {
  "use strict";

  const realFetch = window.fetch.bind(window);

  const json = (body, status = 200) =>
    new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });

  const toUnixSeconds = (iso) => Math.floor(new Date(iso).getTime() / 1000);

  // ---- stations ----------------------------------------------------------------------------
  const oldStation = (s) => ({
    ...s,
    available_bikes: s.availableBikes,
    available_stands: s.availableStands,
    available_bike_stands: s.availableStands,
    total_stands: s.totalStands,
    bike_stands: s.totalStands,
  });

  async function stations() {
    const res = await realFetch("/api/v1/stations");
    if (!res.ok) return res;
    return json((await res.json()).map(oldStation));
  }

  // ---- ML forecast -------------------------------------------------------------------------
  const fmtLocal = (iso) => iso.replace("T", " ").slice(0, 16);   // "2026-10-28T18:00:00" -> "2026-10-28 18:00"

  async function forecast(url) {
    const number = url.searchParams.get("number");
    const hours = url.searchParams.get("hours") || "8";
    const res = await realFetch(`/api/v1/stations/${encodeURIComponent(number)}/forecast?hours=${encodeURIComponent(hours)}`);
    if (!res.ok) return res;
    const f = await res.json();
    if (f.degraded) return json({ error: "Forecast temporarily unavailable" }, 503);
    const p = f.predictions;
    return json({
      number: f.number,
      times: p.map((x) => fmtLocal(x.time)),
      predicted_bikes: p.map((x) => x.bikes),
      predicted_docks: p.map((x) => x.docks),
      predicted_temp: p.map((x) => x.temperature),
      predicted_humidity: p.map((x) => x.humidity),
      predicted_pressure: p.map((x) => x.pressure),
      predicted_rain: p.map((x) => (x.raining ? 1 : 0)),
    });
  }

  // ---- weather -----------------------------------------------------------------------------
  const oldWeather = (w) => ({
    dt: toUnixSeconds(w.time),
    future_dt: toUnixSeconds(w.time),
    temp: w.temperature,
    feels_like: w.feelsLike,
    humidity: w.humidity,
    pressure: w.pressure,
    wind_speed: w.windSpeed,
    wind_gust: null,
    rain_1h: w.rainMm,
    rain_3h: w.rainMm,
    pop: w.rainProbability,
    weather_desc: w.description,
  });

  async function currentWeather(asList) {
    const res = await realFetch("/api/v1/weather/current");
    if (!res.ok) return json({ error: "Weather unavailable" }, res.status);
    const w = oldWeather(await res.json());
    return json({ weather: asList ? [w] : w });
  }

  async function hourlyWeather(url) {
    const limit = Math.min(40, Math.max(1, parseInt(url.searchParams.get("limit") || "8", 10)));
    const res = await realFetch(`/api/v1/weather/forecast?limit=${limit}`);
    if (!res.ok) return json({ error: "Weather unavailable", hourly: [] }, res.status);
    return json({ hourly: (await res.json()).map(oldWeather) });
  }

  // ---- journey -----------------------------------------------------------------------------
  const oldLeg = (l) => ({
    distance_text: l.distanceText,
    distance_m: l.distanceMeters,
    duration_text: l.durationText,
    duration_s: l.durationSeconds,
    polyline: l.polyline,
    steps: (l.steps || []).map((s) => ({
      instruction: s.instruction || "",
      distance: s.distanceText || "",
      duration: s.durationText || "",
    })),
  });

  // "2026-10-30 08:00:00" -> "2026-10-30T08:00" (Dublin local time, which is what the backend expects)
  const toDepartAt = (time) => String(time).trim().replace(" ", "T").slice(0, 16);

  // With a prediction, the station cards show the expected numbers at that time instead of the live ones.
  const withOutlook = (station, outlook) =>
    outlook && outlook.predicted
      ? {
          ...station,
          available_bikes: Math.round(outlook.bikes),
          available_stands: Math.round(outlook.docks),
          available_bike_stands: Math.round(outlook.docks),
        }
      : station;

  const postJson = (path, body) =>
    realFetch(path, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) });

  async function journey(init, predicted) {
    const body = JSON.parse(init.body || "{}");
    const res = predicted
      ? await postJson("/api/v1/journeys/predict", { start: body.start, end: body.end, departAt: toDepartAt(body.time) })
      : await postJson("/api/v1/journeys", { start: body.start, end: body.end });
    const data = await res.json();
    if (!res.ok) return json({ error: data.detail || data.title || "Route unavailable" }, res.status);

    const plan = predicted ? data.journey : data;
    const pickupOutlook = predicted ? data.pickupOutlook : null;
    const dropoffOutlook = predicted ? data.dropoffOutlook : null;
    // The backend falls back to live data when the forecast service is down; only say "predicted" if it really was.
    const usedPrediction = Boolean(predicted && (pickupOutlook.predicted || dropoffOutlook.predicted));
    return json({
      start: { ...plan.start, address: body.start },
      end: { ...plan.end, address: body.end },
      pickup_station: withOutlook(oldStation(plan.pickup), pickupOutlook),
      dropoff_station: withOutlook(oldStation(plan.dropoff), dropoffOutlook),
      legs: {
        walk_to_bike: oldLeg(plan.walkToPickup),
        cycle: oldLeg(plan.cycle),
        walk_from_bike: oldLeg(plan.walkFromDropoff),
      },
      summary: { total_distance: plan.totalDistanceMeters, total_duration_s: plan.totalDurationSeconds },
      prediction_mode: usedPrediction,
      weather_warning: null,
    });
  }

  // ---- routing table -----------------------------------------------------------------------
  window.fetch = function (input, init = {}) {
    const raw = typeof input === "string" ? input : input.url;
    const url = new URL(raw, window.location.href);
    if (url.origin !== window.location.origin) return realFetch(input, init);
    const path = url.pathname.replace(/\/+$/, "");
    const method = (init.method || "GET").toUpperCase();

    if (path === "/api/stations" || path === "/journey/api/availability") return stations();
    if (path === "/api/stations/predict") return forecast(url);
    if (path === "/api/weather/openweather/current") return currentWeather(false);
    if (path === "/api/weather/db/current") return currentWeather(true);
    if (path === "/api/weather/db/hourly" || path === "/api/weather/openweather/forecast3h") return hourlyWeather(url);
    if (path === "/api/chat") return realFetch("/api/v1/chat", init);
    if (path === "/journey/api/config") return realFetch("/api/v1/journeys/config");
    if (path === "/journey/api/route" && method === "POST") return journey(init, false);
    if (path === "/journey/api/route/predict" && method === "POST") return journey(init, true);
    return realFetch(input, init);
  };
})();
