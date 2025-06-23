FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /app
COPY . .
RUN apt-get update && apt-get install -y maven && \
    mvn clean install -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/stat-server/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]