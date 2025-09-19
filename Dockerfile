FROM openjdk:21-slim

# Installer Nginx
RUN apt-get update && \
    apt-get install -y nginx && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /var/logs/crud && chmod -R 777 /var/logs/crud

EXPOSE 80

ENTRYPOINT ["/entrypoint.sh"]
