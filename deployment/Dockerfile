# Stage 1: Build
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .
COPY main.gradle .
COPY applications applications
COPY domain domain
COPY infrastructure infrastructure
RUN chmod +x ./gradlew && ./gradlew bootJar -x test -x validateStructure

# Stage 2: Runtime
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN groupadd -r appuser && useradd -r -g appuser appuser
COPY --from=builder /app/applications/app-service/build/libs/*.jar app.jar
RUN chown -R appuser:appuser /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=70.0", "-jar", "app.jar"]