FROM azul/zulu-openjdk-alpine:11.0.10-jre

LABEL maintainer="Ernesto J. Perez <ernestojpg@gmail.com>"

RUN mkdir -p /opt/load-test-server/conf

COPY docker/load-test-server.sh /opt/load-test-server/load-test-server.sh
COPY src/main/resources/log4j2.xml /opt/load-test-server/conf/log4j2.xml
COPY target/load-test-server-*-app.jar /opt/load-test-server/load-test-server.jar

# Use "exec" form so that it runs as PID 1 (useful for graceful shutdown)
CMD ["/opt/load-test-server/load-test-server.sh"]
