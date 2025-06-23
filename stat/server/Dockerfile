FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

COPY ../../stat/pom.xml ../
COPY ../../stat/dto ../dto
COPY ../../stat/client ../client
COPY . .

RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
