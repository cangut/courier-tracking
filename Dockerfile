# --- Build stage: compile the multi-module reactor and repackage the boot jar ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy poms first for better layer caching, then sources.
COPY pom.xml .
COPY domain/pom.xml domain/pom.xml
COPY application/pom.xml application/pom.xml
COPY infrastructure/pom.xml infrastructure/pom.xml
COPY bootstrap/pom.xml bootstrap/pom.xml
COPY domain/src domain/src
COPY application/src application/src
COPY infrastructure/src infrastructure/src
COPY bootstrap/src bootstrap/src

RUN mvn -q -B clean package -DskipTests


# --- Runtime stage: slim JRE with just the executable jar ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/bootstrap/target/courier-tracking.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]