FROM ubuntu:20.04
# image used to precompile the necessary libraries, to save time on deployments

# install required packages
RUN apt update -y && \
	apt install openvpn openssl libssl-dev ca-certificates vim tar iptables net-tools openjdk-11-jdk libc6 make gcc -y

# copy plugin sources
COPY charon-plugin /src/charon-plugin
COPY Makefile /src/
# compile the plugin inside the container
WORKDIR /src
RUN make build-c-notests

WORKDIR /etc/openvpn/server