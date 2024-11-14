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

java_options="-server -XX:MaxRAMPercentage=75.0 -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -Xss100m \
                 -XX:FlightRecorderOptions=stackdepth=256 -Djava.security.egd=file:/dev/urandom \
                  -Dliquibase.changelogLockPollRate=1 -Duser.timezone=\"UTC\""

if [[ $DD_ENV == "aws" ]]; then
  echo "Starting marketplace-api for spring-profiles-active ${SPRING_PROFILES_ACTIVE} and Datadog env ${DD_ENV}"
  java_options="${java_options} -Ddd.profiling.enabled=true -Ddd.logs.injection=true"
  java -javaagent:/webapp/dd-java-agent.jar \
    $java_options \
    -jar webapp/marketplace-api.jar \
    --spring.profiles.active=$SPRING_PROFILES_ACTIVE
else
  echo "Starting marketplace-api for spring-profiles-active ${SPRING_PROFILES_ACTIVE}"
  java $java_options \
    -jar webapp/marketplace-api.jar \
    --spring.profiles.active=$SPRING_PROFILES_ACTIVE
fi