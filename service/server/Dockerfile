FROM amazoncorretto:21-alpine
WORKDIR /app
COPY  service/server/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]