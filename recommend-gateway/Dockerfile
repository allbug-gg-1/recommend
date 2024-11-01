FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/recommend-gateway-0.0.1-SNAPSHOT.jar /app/recommend-gateway.jar
EXPOSE 8112
ENTRYPOINT ["java", "-jar", "/app/recommend-gateway.jar"]
