# --- Build stage: compile and package the Spring Boot jar --------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies first (only re-runs when pom.xml changes).
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline || true

# Build the application.
COPY src ./src
RUN mvn -q -DskipTests clean package

# --- Runtime stage: small JRE image ------------------------------------------
FROM eclipse-temurin:17-jre
WORKDIR /app

# The Spring Boot repackaged jar (the *.jar.original is ignored by this glob).
COPY --from=build /app/target/*.jar app.jar

# Render/Railway/Fly inject the real port via $PORT; our app reads ${PORT:3000}.
EXPOSE 3000

# Default to the prod profile (file-backed H2, console off). Override with
# SPRING_PROFILES_ACTIVE if needed (e.g. "prod,secure").
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]
