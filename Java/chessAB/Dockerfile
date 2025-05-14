# ====== Stage 1: Build the application ======
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set work directory
WORKDIR /build

# Copy pom.xml and download dependencies first (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Package the app
RUN mvn package -DskipTests

# ====== Stage 2: Runtime with just Java 21 ======
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy built jar from stage 1
COPY --from=builder /build/target/*.jar app.jar

RUN rm -rf /build /src

# Expose port 8080
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
