FROM openjdk:17-jdk-alpine
WORKDIR /app
RUN mkdir -p /app/recommend-gateway && chmod -R 777 /app/recommend-gateway
COPY target/recommend-gateway-0.0.1-SNAPSHOT.jar /app/recommend-gateway/recommend-gateway.jar
EXPOSE 8112
ENTRYPOINT ["java", "-jar", "/app/recommend-gateway/recommend-gateway.jar"]
