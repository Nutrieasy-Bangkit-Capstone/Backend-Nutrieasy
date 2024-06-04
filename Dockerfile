# Use the official maven/Java 11 image to create a build artifact.
# https://hub.docker.com/_/maven
FROM maven:3-jdk-11-slim AS build-env

# Set the working directory to /app
WORKDIR /app
# Copy the pom.xml file to download dependencies
COPY pom.xml ./
# Copy local code to the container image.
COPY src ./src

# Download dependencies and build a release artifact.

# Download dependencies and build a release artifact.
RUN mvn clean install -DskipTests
RUN mvn package -DskipTests

# Use OpenJDK for base image.
# https://hub.docker.com/_/openjdk
# https://docs.docker.com/develop/develop-images/multistage-build/#use-multi-stage-builds
FROM openjdk:11.0.16-jre-slim
# Add a volume pointing to /tmp
VOLUME /tmp

# Copy the jar to the production image from the builder stage.
COPY --from=build-env /app/target/nutrieasy-backend-service-*.jar /nutrieasy-backend-service.jar


COPY src/main/resources/service-account.json /app/service-account.json
# Run the web service on container startup.
CMD ["java","-Djava.security.egd=file:/dev/./urandom", "-jar", "/nutrieasy-backend-service.jar"]
