#!/usr/bin/env bash
docker build --tag=solr-zubtsov .

#docker volume rm solr-volume
#docker volume create solr-volume

#docker run \
# -p 8983:8983 \
#--mount source=solr-volume,target=/opt/solr/server/solr/mycores \
#solr-zubtsov