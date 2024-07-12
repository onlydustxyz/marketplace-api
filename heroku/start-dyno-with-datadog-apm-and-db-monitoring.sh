#!/bin/bash

# Activate Datadog DB monitoring
echo 'init_config:
      instances:
        - dbm: true
          host: $DATABASE_HOST
          port: 5432
          username: datadog
          password: $DD_DATABASE_PWD
' >> /app/.apt/etc/datadog-agent/conf.d/postgres.d/conf.yaml
systemctl restart datadog-agent

wget -O dd-java-agent.jar https://dtdg.co/latest-java-tracer
java -javaagent:dd-java-agent.jar "$@" -XX:FlightRecorderOptions=stackdepth=256 -jar bootstrap/target/marketplace-api.jar