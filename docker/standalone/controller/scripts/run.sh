#!/bin/bash
java -XX:MaxPermSize=200m -jar ${BASE_DIR}/ngrinder-controller/ngrinder-controller-3.3.war --port 80
