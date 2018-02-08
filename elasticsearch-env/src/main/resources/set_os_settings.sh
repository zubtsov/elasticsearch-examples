#!/usr/bin/env bash
groupadd docker
usermod -aG docker $USER
adduser $USER docker

sysctl -w vm.max_map_count=262144

apt-get install curl