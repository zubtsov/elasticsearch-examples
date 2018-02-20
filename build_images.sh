#!/usr/bin/env bash
docker build --tag=solr-zubtsov ./solr-env/src/main/resources/
docker build --tag=logstash-zubtsov-base --network=host -f Dockerfile ./logstash-env/src/main/resources/
docker build --tag=logstash-zubtsov --network=host -f Dockerfile2 ./logstash-env/src/main/resources/