# syntax=docker/dockerfile:1
# Spring Boot backend. Multi-stage build: the first stage compiles and packages with a JDK, the second ships only a JRE and the jar, so the image is smaller.

# The jar is the same on every CPU, so build it on the build machine's own platform (fast even when
# cross-building an arm64 image on an x86 CI runner); only the runtime stage below is per-platform.
FROM --platform=$BUILDPLATFORM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY mvnw pom.xml ./
COPY .mvn .mvn
# Download dependencies first: this layer stays cached while pom.xml is unchanged, so code changes do not re-download them
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q dependency:go-offline
COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q package -DskipTests && cp target/*.jar /app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 1001 spring
USER spring
COPY --from=build /app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
