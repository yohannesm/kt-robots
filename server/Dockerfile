FROM openjdk:11-jre-slim

RUN mkdir /app

COPY ./build/libs/server-dev.jar /app/server-dev.jar

CMD ["java", "-jar", "/app/server-dev.jar"]