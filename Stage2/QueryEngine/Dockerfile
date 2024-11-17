FROM openjdk:21-jdk-slim

WORKDIR /app

COPY resources /app/resources

COPY target/QueryEngine-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
