#!/usr/bin/env bash
#content taken from https://github.com/docker/compose/releases
curl -L https://github.com/docker/compose/releases/download/1.19.0-rc2/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

#TODO: add check
docker-compose --version