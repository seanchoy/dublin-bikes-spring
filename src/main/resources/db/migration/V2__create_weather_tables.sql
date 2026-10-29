-- 天气数据（Part 12）。V1 已经执行过了，所以新表写在 V2 里。

-- 实时天气：每次抓到一条，只增不改（历史记录）
CREATE TABLE weather_current (
    observed_at  TIMESTAMP(6) NOT NULL,
    temperature  DOUBLE       NOT NULL,
    feels_like   DOUBLE       NOT NULL,
    humidity     INT          NOT NULL,
    pressure     INT          NOT NULL,
    wind_speed   DOUBLE       NOT NULL,
    description  VARCHAR(100),
    rain_mm      DOUBLE       NOT NULL,
    PRIMARY KEY (observed_at)
);

-- 天气预报：每个预报时刻一行，重新抓取时用最新的预报覆盖（upsert）
CREATE TABLE weather_forecast (
    forecast_time    TIMESTAMP(6) NOT NULL,
    temperature      DOUBLE       NOT NULL,
    feels_like       DOUBLE       NOT NULL,
    humidity         INT          NOT NULL,
    pressure         INT          NOT NULL,
    wind_speed       DOUBLE       NOT NULL,
    description      VARCHAR(100),
    rain_mm          DOUBLE       NOT NULL,
    rain_probability DOUBLE       NOT NULL,
    fetched_at       TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (forecast_time)
);
