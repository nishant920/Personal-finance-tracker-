# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy project definition and wrapper files first to leverage caching
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Fix potential Windows CRLF line endings and grant execution permission
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Copy application source code
COPY src src

# Build the application package skipping tests
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose backend application port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]


