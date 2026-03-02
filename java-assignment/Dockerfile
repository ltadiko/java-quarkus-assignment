# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
# Copy pom and resolve dependencies (optimized for layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add a non-root user for security
RUN addgroup -S java-assignment && adduser -S java-assignment -G java-assignment

# Copy the entire quarkus-app directory which contains all necessary files and dependencies
COPY --from=build --chown=java-assignment:java-assignment /app/target/quarkus-app /app

USER java-assignment

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]

