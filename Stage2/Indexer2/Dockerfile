FROM openjdk:21-jdk-slim

WORKDIR /app

COPY resources /Indexer2/resources

COPY target/Indexer2-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
