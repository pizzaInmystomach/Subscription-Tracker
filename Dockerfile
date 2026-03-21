# phase 1: Build 
# using maven image to build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# phase 2: Run
# only need JDK runtime environment, no need for Maven, this can reduce the size of the final image
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# copy the compiled jar file from the "build" phase
COPY --from=build /app/target/*.jar app.jar

# execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]