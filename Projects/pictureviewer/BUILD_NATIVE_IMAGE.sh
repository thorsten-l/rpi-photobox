#!/bin/bash

export JAVA_HOME="/opt/nik/17"
export PATH="$JAVA_HOME/bin:$PATH"

java -version

mvn -Pnative spring-boot:build-image
