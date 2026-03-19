# Electrum Subscription Tracker Dockerfile
FROM eclipse-temurin:21-jdk-alpine

# set the working directory in the container
WORKDIR /app

# Copy the jar file from the target directory to the container
# Hint: Ensure that the jar file is built and located in the target directory before building the Docker image.
COPY target/*.jar app.jar

# Execute the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]