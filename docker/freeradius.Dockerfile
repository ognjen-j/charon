FROM alpine:3.12.0

ARG RADIUS_DB_ADDRESS
ARG RADIUS_DB_USERNAME
ARG RADIUS_DB_PASSWORD
ENV RADIUS_DB_ADDRESS=${RADIUS_DB_ADDRESS}
ENV RADIUS_DB_USERNAME=${RADIUS_DB_USERNAME}
ENV RADIUS_DB_PASSWORD=${RADIUS_DB_PASSWORD}

RUN apk --update add freeradius freeradius-mysql freeradius-eap openssl vim bash mysql-client

# copy the modified configuration files
COPY target/configs/radius/mods-available/* /etc/raddb/mods-available/
COPY target/configs/radius/mods-config/ippool/queries.conf /etc/raddb/mods-config/sql/ippool/mysql/
COPY target/configs/radius/mods-config/main/queries.conf /etc/raddb/mods-config/sql/main/mysql/
COPY target/configs/radius/sites-available/default /etc/raddb/sites-available/
COPY target/configs/radius/sites-available/inner-tunnel /etc/raddb/sites-available/
COPY target/configs/radius/clients.conf /etc/raddb/
COPY target/configs/radius/radiusd.conf /etc/raddb/
RUN ln -s /etc/raddb/mods-available/sqlippool /etc/raddb/mods-enabled/sqlippool

# copy the startup script
COPY scripts/wait-for-mysql /scripts/

# disable EAP and MSCHAP modules, since we don't need them
RUN rm -f /etc/raddb/mods-enabled/mschap
RUN rm -f /etc/raddb/mods-enabled/eap
RUN rm -f /etc/raddb/mods-enabled/ntlm_auth


ENTRYPOINT /scripts/wait-for-mysql ${RADIUS_DB_ADDRESS} ${RADIUS_DB_USERNAME} ${RADIUS_DB_PASSWORD} /usr/sbin/radiusd -f -d /etc/raddb
