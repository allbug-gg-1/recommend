FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/cloud-test-0.0.1-SNAPSHOT.jar /app/cloud-test.jar
EXPOSE 8222
ENTRYPOINT ["java", "-jar", "/app/cloud-test.jar"]
