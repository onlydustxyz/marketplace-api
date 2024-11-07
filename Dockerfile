FROM --platform=linux/amd64 openjdk:17-alpine

WORKDIR webapp/
ADD bootstrap/target/marketplace-api.jar webapp/marketplace-api.jar
ADD docker-start-api.sh webapp/docker-start-api.sh
RUN chmod +x webapp/docker-start-api.sh
RUN wget -O dd-java-agent.jar https://dtdg.co/latest-java-tracer
RUN export DD_AGENT_HOST=$(curl http://169.254.169.254/latest/meta-data/local-ipv4)

CMD webapp/docker-start-api.sh