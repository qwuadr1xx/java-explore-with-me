FROM amazoncorretto:21 AS stat-builder
WORKDIR /app

COPY pom.xml .
COPY stat stat
COPY service service

RUN apt-get update && apt-get install -y maven \
  && mvn clean package -DskipTests -pl stat/server

FROM amazoncorretto:21
WORKDIR /app
COPY --from=stat-builder /app/stat/server/target/*.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java","-jar","app.jar"]