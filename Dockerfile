FROM adoptopenjdk/openjdk14:jdk-14.0.1_7-alpine-slim AS builder
WORKDIR /opt/app
COPY ./build.gradle .
COPY ./gradle ./gradle
COPY ./gradlew .
COPY ./settings.gradle .
COPY ./src ./src
RUN ./gradlew build

FROM adoptopenjdk/openjdk14:jre-14.0.1_7-alpine
WORKDIR /opt/app
COPY --from=builder /opt/app/build/libs/rock-paper-scissors.jar ./application.jar
CMD ["java", "-jar", "application.jar"]
