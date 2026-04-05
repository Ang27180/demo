# Etapa 1: compilar dentro de Linux (evita JAR obsoleto del host y diferencias Windows/Linux)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests clean package

# Etapa 2: solo JRE
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
