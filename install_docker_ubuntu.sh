#!/usr/bin/env bash
#remove old docker versions
apt-get remove docker docker-engine docker.io

apt-get update

apt-get install \
	apt-transport-https \
	ca-certificates \
	curl \
	software-properties-common

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -

#verify that we now have the key with this fingerprint by searhing for the last 8 chars
#TODO: add check
apt-key fingerprint 0EBFCD88

add-apt-repository \
	"deb [arch=amd64] https://download.docker.com/linux/ubuntu \
	$(lsb_release -cs) \
	stable"

apt-get update

#install the latest Docker version. For production purposes version must be specified
apt-get install docker-ce

#verify installation. TODO: add check
docker run hello-world
