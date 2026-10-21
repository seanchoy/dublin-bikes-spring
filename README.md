# Dublin Bikes (Spring Boot)

Real-time bike-share planner for Dublin: live station availability, weather, journey planning with ML-predicted availability, and an AI assistant.

Live at **https://bikes.seancai.dev**

This repository is a Spring Boot rewrite of the backend. The frontend (HTML, CSS, JavaScript) is reused unchanged from the original Python/Flask version.

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot |
| Database | MySQL on AWS RDS |
| ML inference | ONNX Runtime (Random Forest models exported from scikit-learn) |
| External APIs | JCDecaux, OpenWeatherMap, Google Maps, Groq |
| Hosting | AWS EC2, nginx, Let's Encrypt |

## Credits

The original Python/Flask version, including the frontend reused here and the forecasting models, was built as a university group project with Shayan Chakraborty, Jialin Wang and Leah Unnikrishnan.
