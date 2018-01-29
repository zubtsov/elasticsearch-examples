#!/usr/bin/env bash
docker build --tag=elasticsearch-zubtsov .

#docker volume rm elasticsearch-volume
docker volume create elasticsearch-volume

docker run \
  -p 9200:9200 -p 9300:9300 \
  --mount source=elasticsearch-volume,target=/usr/share/elasticsearch/data \
  elasticsearch-zubtsov