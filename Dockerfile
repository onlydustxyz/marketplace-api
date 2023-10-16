FROM openjdk:17.0.1-jdk

WORKDIR webapp/
ADD bootstrap/target/marketplace-api.jar webapp/marketplace-api.jar
ADD bootstrap/src/main/resources/application.yaml webapp/application.yaml

CMD java -jar marketplace-api.jar
