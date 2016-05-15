#!/bin/sh
java -XX:MaxPermSize=200m -jar ${BASE_DIR}/ngrinder-*.war --port 80
