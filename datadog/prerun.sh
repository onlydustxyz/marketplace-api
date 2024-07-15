#!/bin/bash

DB_HOST=$(echo $DATABASE_URL | cut -d '@' -f 2 | cut -d ':' -f 1);
DB_USERNAME=$(echo $DATABASE_URL | cut -d '/' -f 3 | cut -d ':' -f 1);
DB_PASSWORD=$(echo $DATABASE_URL | cut -d '/' -f 3 | cut -d ':' -f 2 | cut -d '@' -f 1);
DB_PORT=$(echo $DATABASE_URL | cut -d ':' -f 4 | cut -d '/' -f 1);
DB_NAME=$(echo $DATABASE_URL | cut -d ':' -f 4 | cut -d '/' -f 2);

echo "init_config:
      instances:
        - host: ${DB_HOST}
          username: ${DB_USERNAME}
          password: ${DB_PASSWORD}
          dbm: true
          port: ${DB_PORT}
          dbname: ${DB_NAME}
          ssl: required
          collect_schemas:
            enabled: true
" >> /app/.apt/etc/datadog-agent/conf.d/postgres.d/conf.yaml;

agent-wrapper stop;
agent-wrapper run;