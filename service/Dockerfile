FROM amazoncorretto:21 AS service-builder
WORKDIR /app

COPY pom.xml .
COPY stat stat
COPY service service

RUN apt-get update && apt-get install -y maven \
  && mvn clean package -DskipTests -pl service

FROM amazoncorretto:21
WORKDIR /app
COPY --from=service-builder /app/service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]