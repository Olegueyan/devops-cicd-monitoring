#!/bin/sh
set -e

# If DISABLE_NGINX=true (Cloud Run), run Ktor only.
if [ "$DISABLE_NGINX" = "true" ]; then
  exec java -jar /app/app.jar
fi

# Local/dev: run Ktor then keep nginx in foreground.
java -jar /app/app.jar &
exec nginx -g "daemon off;"
