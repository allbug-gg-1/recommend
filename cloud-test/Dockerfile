FROM openjdk:17-jdk-alpine
WORKDIR /app
RUN mkdir -p /app/cloud-test && chmod -R 777 /app/cloud-test
COPY target/cloud-test-0.0.1-SNAPSHOT.jar /app/cloud-test/cloud-test.jar
EXPOSE 8222
ENTRYPOINT ["java", "-jar", "/app/cloud-test/cloud-test.jar"]
