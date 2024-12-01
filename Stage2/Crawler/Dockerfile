FROM openjdk:21-jdk-slim

WORKDIR /app

COPY resources Crawler/resources

COPY target/Crawler-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
