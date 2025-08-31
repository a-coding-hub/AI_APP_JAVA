# Use OpenJDK 17 (Render supports it well)
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Copy source code
COPY src src

# Make mvnw executable
RUN chmod +x mvnw

# Build the JAR (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# Expose port (Render will override with $PORT)
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "target/AI_APP-0.0.1-SNAPSHOT.jar"]
