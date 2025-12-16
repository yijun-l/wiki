# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set work directory
WORKDIR /app

# Copy dependency file (pom.xml) for caching
COPY pom.xml .

# Copy source code
COPY src ./src

# Package the application (JAR is created in /app/target/)
RUN mvn package -DskipTests

# Stage 2: Run the application (Final, lightweight JRE image)
FROM eclipse-temurin:21-jre-alpine

# Expose the application port (default for Spring Boot)
EXPOSE 8080

# Copy the built JAR file from the 'build' stage
COPY --from=build /app/target/*.jar app.jar

# Define the entrypoint to run the JAR
ENTRYPOINT ["java", "-jar", "/app.jar"]