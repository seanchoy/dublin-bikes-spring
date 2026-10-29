# Dublin Bikes (Spring Boot)

Real-time bike-share planner for Dublin: live station availability, weather, journey planning with ML-predicted availability, and an AI assistant.

Live at **https://bikes.seancai.dev**

This repository is a Spring Boot rewrite of the backend. The frontend (HTML, CSS, JavaScript) is reused unchanged from the original Python/Flask version.

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 25, Spring Boot 4 |
| Database | MySQL on AWS RDS, schema managed by Flyway |
| ML inference | Python FastAPI service serving scikit-learn Random Forest models |
| External APIs | JCDecaux, OpenWeatherMap, Google Maps, Groq |
| Hosting | AWS EC2, nginx, Let's Encrypt |

## Running locally

1. Copy `.env.example` to `.env` and fill in the API keys.
2. Put the model files in `ml-service/models/` (about 2 GB, not stored in git).
3. Start everything, with a local MySQL container:

   ```bash
   docker compose up --build
   ```

The app is at http://localhost:8080 and the API docs at http://localhost:8080/swagger-ui.html.

Tests use an in-memory H2 database and need neither Docker nor API keys: `./mvnw test`.

## Using AWS RDS

1. On the RDS instance, create a database and user for this app. Do not reuse the Flask project's databases: their tables have the same names but a different structure, and Flyway refuses to run on a non-empty schema.

   ```sql
   CREATE DATABASE dublin_bikes_spring;
   CREATE USER 'bikes_spring'@'%' IDENTIFIED BY '<strong password>';
   GRANT ALL PRIVILEGES ON dublin_bikes_spring.* TO 'bikes_spring'@'%';
   ```

2. Allow inbound MySQL (port 3306) in the RDS security group from wherever the app runs, ideally the EC2 instance's security group.
3. Set `RDS_HOST`, `RDS_USER` and `RDS_PASSWORD` in `.env`, then start without the local MySQL container:

   ```bash
   docker compose -f compose.yaml -f compose.rds.yaml up --build app ml-service
   ```

Flyway creates the tables on the first start.

## Credits

The original Python/Flask version, including the frontend reused here and the forecasting models, was built as a university group project with Shayan Chakraborty, Jialin Wang and Leah Unnikrishnan.
