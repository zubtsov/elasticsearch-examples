#!/usr/bin/env bash

docker build --tag=elasticsearch-zubtsov .

#docker volume rm elasticsearch-volume
docker volume create elasticsearch-volume

docker run -d \
  --name elasticsearch-zubtsov \
  --mount source=elasticsearch-volume,target=/usr/share/elasticsearch/data