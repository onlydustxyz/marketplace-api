#!/usr/bin/env sh
set -x

if [ -z "$SPRING_PROFILES_ACTIVE" ]; then
  echo "ERROR - Missing spring-profiles-active parameter"
  exit 1
fi

echo "Starting marketplace-api for spring-profiles-active ${SPRING_PROFILES_ACTIVE}"
java_options="-server -XX:MaxRAMPercentage=75.0 -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -Xss100m \
               -XX:FlightRecorderOptions=stackdepth=256 -Djava.security.egd=file:/dev/urandom \
                -Dliquibase.changelogLockPollRate=1 -Duser.timezone=\"Europe/Paris\""
java $java_options \
  -jar webapp/marketplace-api.jar \
  --spring.profiles.active=$SPRING_PROFILES_ACTIVE
