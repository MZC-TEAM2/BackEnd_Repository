# Multi-stage build for Spring Boot application
# Stage 1: Build stage
FROM gradle:8.11.1-jdk21 AS builder

# Set working directory
WORKDIR /app

# Copy gradle files for dependency caching
COPY springboot/build.gradle springboot/settings.gradle ./
COPY springboot/gradle ./gradle
COPY springboot/gradlew ./

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY springboot/src ./src
COPY springboot/config ./config

# Build application (skip tests for faster build)
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-jammy

# Install curl for health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy jar file from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs

# Expose port
EXPOSE 8080

# JVM options for container environment
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]