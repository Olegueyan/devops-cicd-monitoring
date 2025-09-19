#!/bin/sh

# Démarrer Ktor en background
java -jar /app/app.jar &

# Démarrer Nginx au premier plan
nginx -g "daemon off;"
