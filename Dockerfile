# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
# Copy the Maven wrapper files and the pom.xml
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
# Grant execute permission to the Maven wrapper script
RUN chmod +x ./mvnw
# Copy the application source code
COPY src ./src
# Build the project and create the executable JAR
RUN ./mvnw clean install -DskipTests

# Stage 2: Create the final image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Copy the executable JAR from the builder stage
COPY --from=builder /app/target/mpl-0.0.1-SNAPSHOT.jar .
# Expose the application port
EXPOSE 8080
# Set the entry point to run the application
ENTRYPOINT ["java", "-jar", "mpl-0.0.1-SNAPSHOT.jar"]