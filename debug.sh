#!/bin/bash
if [[ -z ${MAVEN_OPTS} ]]; then
    echo "The environment variable 'MAVEN_OPTS' is not set, setting it for you";
    MAVEN_OPTS="-Xms256m -Xmx2G"
fi
#MAVEN_OPTS="$MAVEN_OPTS -Djavax.net.ssl.trustStore=./java_cacerts"
echo "MAVEN_OPTS is set to '$MAVEN_OPTS'";
mvnDebug clean install alfresco:run
