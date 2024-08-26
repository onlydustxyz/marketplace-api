#!/bin/bash

if [ -n "`docker ps -f name=marketplace_db_with_IT_dump -q`" ]
then
	docker stop marketplace_db_with_IT_dump
	echo "Waiting for container to stop"
	while [ -n "`docker ps -f name=marketplace_db_with_IT_dump -q`" ]; do sleep 1; done
fi

docker container prune -f
docker volume prune -f
docker-compose -f ./docker-compose-IT-db.yaml up -d

while [ `docker ps -f name=marketplace_db_with_IT_dump | grep -c healthy` -eq 0 ]; do sleep 1; done
echo Container is healthy

sleep 3

docker exec -it marketplace_db_with_IT_dump "/scripts/restore_db.sh" true

