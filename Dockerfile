FROM --platform=linux/amd64 openjdk:17-alpine

WORKDIR webapp/
ADD bootstrap/target/marketplace-api.jar webapp/marketplace-api.jar

CMD java -Dspring.profiles.active=api -jar webapp/marketplace-api.jar