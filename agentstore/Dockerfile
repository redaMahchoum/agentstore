FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/agentstore.jar /app/agentstore.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/agentstore.jar"] 