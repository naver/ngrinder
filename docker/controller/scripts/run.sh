#!/usr/bin/env bash
echo "wait a while extracting war files... It takes time for the first run."
java -jar ${BASE_DIR}/ngrinder-*.war --port 80
