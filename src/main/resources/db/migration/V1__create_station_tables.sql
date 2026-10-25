-- Flyway 会在应用启动时按版本号顺序执行 db/migration 下的脚本，并记录在 flyway_schema_history 表里。
-- 已经执行过的脚本不要再改；要改表结构就新建 V2__xxx.sql。
-- 这里的 SQL 同时兼容 MySQL 和 H2（测试用的内存数据库）。

CREATE TABLE station (
    number      INT          NOT NULL,
    name        VARCHAR(255) NOT NULL,
    address     VARCHAR(255),
    lat         DOUBLE       NOT NULL,
    lng         DOUBLE       NOT NULL,
    banking     BOOLEAN      NOT NULL,
    bike_stands INT          NOT NULL,
    status      VARCHAR(32),
    PRIMARY KEY (number)
);

CREATE TABLE availability (
    number                INT          NOT NULL,
    last_update           TIMESTAMP(6) NOT NULL,
    available_bikes       INT          NOT NULL,
    available_bike_stands INT          NOT NULL,
    status                VARCHAR(32),
    PRIMARY KEY (number, last_update),
    CONSTRAINT fk_availability_station FOREIGN KEY (number) REFERENCES station (number)
);
