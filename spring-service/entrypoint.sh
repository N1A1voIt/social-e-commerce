#!/bin/bash

WAR_FILE=/usr/local/tomcat/webapps/my-app.war
DEST_DIR=/usr/local/tomcat/webapps/ROOT

mkdir -p "$DEST_DIR"
cd "$DEST_DIR"
jar -xf "$WAR_FILE"

rm "$WAR_FILE"

catalina.sh run
