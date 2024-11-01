FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/recommend-api-0.0.1-SNAPSHOT.jar /app/recommend-api.jar
EXPOSE 8111
ENTRYPOINT ["java", "-jar", "/app/recommend-api.jar"]
