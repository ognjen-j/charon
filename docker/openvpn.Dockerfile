FROM ognjenj/charon-openvpn-base:1.7

ARG CHARON_ACCT_VERSION=1.0
ENV CHARON_ACCT_VERSION=${CHARON_ACCT_VERSION}
ARG OVPN_DNS_1
ARG OVPN_DNS_2
ENV OVPN_DNS_1=${OVPN_DNS_1}
ENV OVPN_DNS_2=${OVPN_DNS_2}

# initialize the plugin
COPY target/charon-acct/charon.conf /etc/openvpn/server/
COPY target/charon-acct/charon-acct /etc/openvpn/server/
COPY target/charon-acct/log4j2.xml /etc/openvpn/server/
COPY target/charon-acct/lib /etc/openvpn/server/lib
COPY target/charon-acct/charon-acct-${CHARON_ACCT_VERSION}.jar /etc/openvpn/server/

# copy configurations for OpenVPN
COPY target/configs/openvpn.conf /etc/openvpn/server/
RUN cp /src/target/charon-acct/charon.so /etc/openvpn/server/

# copy startup script
COPY scripts/openvpn-run /app/

# create the necessary local directories
RUN mkdir --parent /var/log/openvpn/
RUN mkdir --parent /opt/certificates/ovpn
RUN mkdir --parent /opt/certificates/ovpn/crl
RUN mkdir --parent /etc/openvpn/ccd
RUN mkdir --parent /etc/openvpn/tmp

# setup DNS resolvers
RUN echo "nameserver ${OVPN_DNS_1}" >> /etc/resolv.conf
RUN echo "nameserver ${OVPN_DNS_2}" >> /etc/resolv.conf

ENTRYPOINT /bin/bash /app/openvpn-run
