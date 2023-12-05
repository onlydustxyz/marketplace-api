#!/bin/bash

wget -O dd-java-agent.jar https://dtdg.co/latest-java-tracer
java -javaagent:dd-java-agent.jar -XX:FlightRecorderOptions=stackdepth=256 -jar bootstrap/target/marketplace-api.jar