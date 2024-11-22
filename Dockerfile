FROM --platform=linux/amd64 openjdk:21-jdk

WORKDIR webapp/

ADD bootstrap/target/marketplace-api.jar .
ADD docker-start.sh .

RUN wget -O dd-java-agent.jar https://dtdg.co/latest-java-tracer

CMD ./docker-start.sh marketplace-api.jar
