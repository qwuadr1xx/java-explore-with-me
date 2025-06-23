FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY pom.xml .
COPY stat stat
COPY service service
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests \

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /app/stat/server/target/*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java","-jar","app.jar"]