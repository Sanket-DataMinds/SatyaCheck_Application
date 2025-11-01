FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY src src

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp

# Copy the service account key file (needs to be in the same directory as the Dockerfile)
COPY satyacheck-backend-281deb66d0d0.json /app/satyacheck-backend-281deb66d0d0.json

# Set environment variables
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/satyacheck-backend-281deb66d0d0.json
ENV SPRING_PROFILES_ACTIVE=prod

COPY --from=build /workspace/app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]