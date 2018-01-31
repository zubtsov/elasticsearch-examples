#!/usr/bin/env bash
docker build --tag=logstash-zubtsov .
docker run -p 9600:9600 logstash-zubtsov