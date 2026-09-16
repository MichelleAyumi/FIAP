FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY contracts/pom.xml contracts/pom.xml
COPY scheduling-service/pom.xml scheduling-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY contracts/src contracts/src
COPY scheduling-service/src scheduling-service/src
COPY notification-service/src notification-service/src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S hospital && adduser -S hospital -G hospital
WORKDIR /app
ARG SERVICE=scheduling-service
COPY --from=build /workspace/${SERVICE}/target/${SERVICE}-1.0.0-SNAPSHOT.jar /app/app.jar
USER hospital
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "/app/app.jar"]
