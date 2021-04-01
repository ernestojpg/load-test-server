#!/bin/sh

if [ -z "$JAVA_OPTS" ]
then
    JAVA_OPTS="-Dlog4j.configurationFile=/opt/load-test-server/conf/log4j2.xml"
fi

COMMAND="java ${JAVA_OPTS} -jar /opt/load-test-server/load-test-server.jar $@"

echo "Running Load Test Server ...."
echo ${COMMAND}

# Execute the Java application replacing the current shell process
exec ${COMMAND}
