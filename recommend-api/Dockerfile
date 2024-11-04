FROM openjdk:17-jdk-alpine
WORKDIR /app
RUN mkdir -p /app/recommend-api && chmod -R 777 /app/recommend-api
COPY target/recommend-api-0.0.1-SNAPSHOT.jar /app/recommend-api/recommend-api.jar
EXPOSE 8111
ENTRYPOINT ["java", "-jar", "/app/recommend-api/recommend-api.jar"]
