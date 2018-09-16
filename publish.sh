#!/usr/bin/env bash

# Build
./gradlew clean assemble

# Upload
scp build/libs/banana-jack-server-1.0-SNAPSHOT.jar igor@ilaborie.org:/home/igor

echo "You need to 'ssh igor@ilaborie.org'"
echo "then kill previous process (jps)"
echo "And run with 'java -jar banana-jack-server-1.0-SNAPSHOT.jar &'"