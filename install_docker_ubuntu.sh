#remove old docker versions
sudo apt-get remove docker docker-engine docker.io

sudo apt-get update

sudo apt-get install \
	apt-transport-https \
	ca-certificates \
	curl \
	software-properties-common

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -

#verify that we now have the key with this fingerprint by searhing for the last 8 chars
#TODO: add check
sudo apt-key fingerprint 0EBFCD88

sudo add-apt-repository \
	"deb [arch=amd64] https://download.docker.com/linux/ubuntu \
	$(lsb_release -cs) \
	stable"

sudo apt-get update

#install the latest Docker version. For production purposes version must be specified
sudo apt-get install docker-ce

#verify installation. TODO: add check
sudo docker run hello-world
