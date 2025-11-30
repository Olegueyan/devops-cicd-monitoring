FROM gradle:8.10.2-jdk21 AS build
WORKDIR /workspace

COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY settings.gradle.kts settings.gradle.kts
COPY gradle.properties gradle.properties
COPY gradle gradle
COPY app app

RUN chmod +x gradlew && ./gradlew :app:clean :app:build -x test

FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu

RUN apt-get update && \
    apt-get install -y nginx && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /var/logs/crud && chmod -R 777 /var/logs/crud

WORKDIR /app
COPY --from=build /workspace/app/build/libs/app.jar /app/app.jar
COPY entrypoint.sh /entrypoint.sh
COPY nginx.conf /etc/nginx/nginx.conf

RUN chmod +x /entrypoint.sh

EXPOSE 80 8080

ENTRYPOINT ["/entrypoint.sh"]
