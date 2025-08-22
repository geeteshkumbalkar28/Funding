# Use Maven with OpenJDK 17 as the base image
FROM maven:3-openjdk-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first (for better layer caching)
COPY pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Use a valid runtime image instead of openjdk:17-jre-slim
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/donorbox-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port that the application will run on
EXPOSE 8080

# Set environment variables for production
ENV SPRING_PROFILES_ACTIVE=production

# Run the application
CMD ["java", "-jar", "app.jar"]
