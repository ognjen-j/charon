FROM openjdk:11.0.8-jre

ARG RADIUS_DB_ADDRESS
ARG RADIUS_DB_USERNAME
ARG RADIUS_DB_PASSWORD
ARG CHARON_WEB_VERSION=1.0
ENV CHARON_WEB_VERSION=${CHARON_WEB_VERSION}
ENV RADIUS_DB_ADDRESS=${RADIUS_DB_ADDRESS}
ENV RADIUS_DB_USERNAME=${RADIUS_DB_USERNAME}
ENV RADIUS_DB_PASSWORD=${RADIUS_DB_PASSWORD}

COPY target/charon-web/lib /app/lib/
COPY target/charon-web/charon-web-${CHARON_WEB_VERSION}.jar /app/
COPY scripts/wait-for-mysql /scripts/

RUN apt-get update -y
RUN apt-get install mycli -y

ENTRYPOINT /scripts/wait-for-mysql ${RADIUS_DB_ADDRESS} ${RADIUS_DB_USERNAME} ${RADIUS_DB_PASSWORD} java -cp /app/lib/*:/app/charon-web-${CHARON_WEB_VERSION}.jar ognjenj.charon.web.config.CharonWebApplication