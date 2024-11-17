# Usa una imagen ligera de Java
FROM openjdk:21-jdk-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia la carpeta resources en el directorio de trabajo del contenedor
COPY resources /app/resources

# Copia el JAR generado en el contenedor
COPY target/Indexer2-1.0-SNAPSHOT.jar app.jar

# Comando para ejecutar el JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
