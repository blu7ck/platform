FROM openjdk:21-jre-slim

WORKDIR /app
COPY target/topluluk-platform-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]