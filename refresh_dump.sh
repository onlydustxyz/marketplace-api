#!/bin/bash

set -e  # Exit immediately if a command exits with a non-zero status

DOCKER_CONTAINER_NAME="marketplace_db_with_IT_dump"
DOCKER_COMPOSE_FILE="docker-compose-IT-db.yaml"
DATABASE_URL="jdbc:postgresql://localhost:5433/marketplace_db"
DATABASE_USERNAME="test"
DATABASE_PASSWORD="test"

# Parse command line arguments
unset -v SKIP_RESET
unset -v SKIP_MIGRATIONS
unset -v SKIP_EXPORT

while [[ $# -gt 0 ]]; do
  case $1 in
    --skip-reset)
      SKIP_RESET=1
      shift
      ;;
    --skip-migrations)
      SKIP_MIGRATIONS=1
      shift
      ;;
    --skip-export)
      SKIP_EXPORT=1
      shift
      ;;
    *)
      echo "Unknown option: $1" >&2
      echo "Usage: $0 [--skip-reset] [--skip-migrations] [--skip-export]" >&2
      exit 1
      ;;
  esac
done

reset_dump() {  
  if [ -n "`docker ps -f name=$DOCKER_CONTAINER_NAME -q`" ]
  then
    docker stop $DOCKER_CONTAINER_NAME
    echo "Waiting for container to stop"
    while [ -n "`docker ps -f name=$DOCKER_CONTAINER_NAME -q`" ]; do sleep 1; done
  fi

  docker container prune -f
  docker volume prune -f
  docker-compose -f $DOCKER_COMPOSE_FILE up -d

  while [ `docker ps -f name=$DOCKER_CONTAINER_NAME | grep -c healthy` -eq 0 ]; do sleep 1; done
  echo "Container is healthy"

  sleep 3

  docker exec -it $DOCKER_CONTAINER_NAME "/scripts/restore_db.sh" true
}

run_migrations() {
  # First compile the project to ensure all dependencies are available
  mvn compile

  (
      cd infrastructure/postgres-adapter || return 1
      mvn liquibase:update \
        -Dliquibase.url=$DATABASE_URL \
        -Dliquibase.username=$DATABASE_USERNAME \
        -Dliquibase.password=$DATABASE_PASSWORD \
        -Dliquibase.changeLogFile=src/main/resources/db/changelog/db.changelog-master.yaml \
        -Dliquibase.driver=org.postgresql.Driver
  )
}

export_dump() {
    docker exec -it $DOCKER_CONTAINER_NAME "/scripts/dump_db.sh"
}

# Only run reset_dump if not skipped
if [ -z "$SKIP_RESET" ]; then
  echo "Running reset dump..."
  if ! reset_dump; then
    echo "Failed to reset dump" >&2
    exit 1
  fi
  echo "Reset dump completed successfully"
else
  echo "Skipping reset dump step"
fi

# Only run migrations if not skipped
if [ -z "$SKIP_MIGRATIONS" ]; then
  echo "Running migrations..."
  if ! run_migrations; then
    echo "Failed to run migrations" >&2
    exit 1
  fi
  echo "Migrations completed successfully"
else
  echo "Skipping migrations step"
fi

# Only run export if not skipped
if [ -z "$SKIP_EXPORT" ]; then
  echo "Running export..."
  if ! export_dump; then
    echo "Failed to export dump" >&2
    exit 1
  fi
  echo "Export completed successfully"
else
  echo "Skipping export step"
fi
