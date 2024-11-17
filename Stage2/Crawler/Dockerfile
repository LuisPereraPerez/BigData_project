# Usa una imagen ligera de Java
FROM openjdk:21-jdk-slim

# Establece el directorio de trabajo
WORKDIR /app

# Copia la carpeta resources de este módulo
COPY resources /app/resources

# Copia el JAR en el contenedor
COPY target/Crawler-1.0-SNAPSHOT.jar app.jar

# Comando para ejecutar el JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
