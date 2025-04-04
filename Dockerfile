FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/wallet-latest.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]