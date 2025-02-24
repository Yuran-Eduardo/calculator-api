FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/rest-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]
