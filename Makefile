MAKE_TEST_OPTIONS=-lcmocka -lssl -lcrypto -lpthread -fPIC
MAKE_OPTIONS=-Wall -shared -lssl -lcrypto -lpthread -fPIC
ADDITIONAL_LD_OPTIONS=-lm
SOURCES=charon-plugin/src/charon.c charon-plugin/src/helpers.c charon-plugin/src/network.c
PERSISTENT_VOLUME_DIRECTORY=`cat ./environment.conf | grep -E "^PERSISTENT_VOLUME_DIRECTORY" | cut -d= -f2`

clean:
	rm -rf target/*

destroy:
	rm -rf ${PERSISTENT_VOLUME_DIRECTORY}/*

build-java-clean:
	rm -rf charon-web/target/*
	rm -rf charon-acct/target/*

create-directories:
	mkdir --parents target/tests
	mkdir --parent target/charon-web
	mkdir --parent target/charon-acct
	mkdir --parent target/ca/private
	mkdir --parent target/ovpn
	mkdir --parent ${PERSISTENT_VOLUME_DIRECTORY}/db
	mkdir --parent ${PERSISTENT_VOLUME_DIRECTORY}/dbinit
	mkdir --parent ${PERSISTENT_VOLUME_DIRECTORY}/downloads
	mkdir --parent ${PERSISTENT_VOLUME_DIRECTORY}/ca/private
	mkdir --parent ${PERSISTENT_VOLUME_DIRECTORY}/ovpn/crl

build-java-no-tests: build-java-clean create-directories
	mvn install -DskipTests
	cp -r charon-web/target/lib target/charon-web/
	cp charon-web/target/*.jar target/charon-web/
	cp -r charon-acct/target/lib target/charon-acct/
	find charon-acct/target -maxdepth 1 -type f -exec cp {} target/charon-acct/ \;

build-c-tests: create-directories ${SOURCES}

	gcc -g -Icharon-plugin/tests/ -o target/tests/test_runner charon-plugin/tests/test_runner.c ${MAKE_TEST_OPTIONS} ${ADDITIONAL_LD_OPTIONS}

build-c-notests: ${SOURCES}
	mkdir --parent target/charon-acct
	gcc -I. -Iinclude/ -o target/charon-acct/charon.so ${MAKE_OPTIONS} ${SOURCES} ${ADDITIONAL_LD_OPTIONS}

build-all-tests: clean build-c-tests build-java-no-tests ${SOURCES}
	cp charon-plugin/tests/charon.conf target/tests/
	cp target/charon-acct/*.jar target/tests/
	cp -r target/charon-acct/lib target/tests/
	cp target/charon-acct/charon-acct target/tests/

test:
	target/tests/test_runner

all: build-java-clean build-java-no-tests create-directories
	cp -r configs target/
	cp docker-compose.template.yaml docker-compose.yaml
	./scripts/render-configurations ./environment.conf ./target
	# generate the PKI material
	./scripts/initialize-ca ./environment.conf ./target
	cp target/ca/ca.pem ${PERSISTENT_VOLUME_DIRECTORY}/ovpn/
	cp target/ca/ovpn.key ${PERSISTENT_VOLUME_DIRECTORY}/ovpn/
	cp target/ca/*.pem ${PERSISTENT_VOLUME_DIRECTORY}/ovpn/
	cp target/ca/charon-web.p12 ${PERSISTENT_VOLUME_DIRECTORY}/ovpn/
	cp target/ca/private/ca.key ${PERSISTENT_VOLUME_DIRECTORY}/ca/private
	# merge the DB init scripts in the order in which they have to be executed
	rm -rf ${PERSISTENT_VOLUME_DIRECTORY}/dbinit/*
	./scripts/fill-ippool ./environment.conf ./target
	cat target/configs/sql/db_create.sql >> ${PERSISTENT_VOLUME_DIRECTORY}/dbinit/01_db_create.sql
	echo "" >> ${PERSISTENT_VOLUME_DIRECTORY}/dbinit/01_db_create.sql
	cat target/configs/radius/mods-config/main/schema.sql >> ${PERSISTENT_VOLUME_DIRECTORY}/dbinit/01_db_create.sql
	echo "" >> ${PERSISTENT_VOLUME_DIRECTORY}/dbinit/01_db_create.sql
	cat target/configs/radius/mods-config/ippool/schema.sql >> ${PERSISTENT_VOLUME_DIRECTORY}/dbinit/01_db_create.sql
	echo "" >> ${PERSISTENT_VOLUME_DIRECTORY}/dbinit/01_db_create.sql
	cat target/configs/sql/ippool_fill.sql >> ${PERSISTENT_VOLUME_DIRECTORY}/dbinit/01_db_create.sql
	echo "" >> ${PERSISTENT_VOLUME_DIRECTORY}/dbinit/01_db_create.sql
	cat target/configs/sql/user_create.sql >> ${PERSISTENT_VOLUME_DIRECTORY}/dbinit/01_db_create.sql
	echo "" >> ${PERSISTENT_VOLUME_DIRECTORY}/dbinit/01_db_create.sql
	cat target/configs/sql/marker_create.sql >> ${PERSISTENT_VOLUME_DIRECTORY}/dbinit/01_db_create.sql
