FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY . /app

WORKDIR /app/service
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]