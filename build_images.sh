#!/usr/bin/env bash
docker build --tag=elasticsearch-zubtsov ./elasticsearch-env/src/main/resources/
docker build --tag=solr-zubtsov ./solr-env/src/main/resources/
docker build --tag=logstash-zubtsov ./logstash-env/src/main/resources/