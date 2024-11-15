#!/usr/bin/env sh

set -x

if [ -z "$SPRING_PROFILES_ACTIVE" ]; then
  echo "ERROR - Missing spring-profiles-active parameter"
  exit 1
fi

if [ -z "$DD_ENV" ]; then
  echo "ERROR - Missing datadog env parameter"
  exit 1
fi

jar=$1
shift

echo "Starting $jar [$SPRING_PROFILES_ACTIVE] in $DD_ENV"

if [ "$DD_ENV" = "production" ]; then
  set -- -javaagent:/webapp/dd-java-agent.jar -Ddd.profiling.enabled=true -Ddd.logs.injection=true "$@"
fi

java "$@" \
  -server \
  -XX:MaxRAMPercentage=75.0 \
  -XX:MaxMetaspaceSize=256m \
  -XX:+UseG1GC \
  -Xss100m \
  -XX:FlightRecorderOptions=stackdepth=256 \
  -Djava.security.egd=file:/dev/urandom \
  -Dliquibase.changelogLockPollRate=1 \
  -Duser.timezone=\"UTC\" \
  -jar "$jar" \
  --spring.profiles.active="$SPRING_PROFILES_ACTIVE"