FROM openjdk:21-jdk-slim

WORKDIR /app

COPY resources /Indexer1/resources

COPY target/Indexer1-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
