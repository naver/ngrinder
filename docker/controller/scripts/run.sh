#!/usr/bin/env bash
echo "Installed build tools."
mvn -version
gradle -version

echo "Wait a while extracting war files... It takes time for the first run."
java -jar -Djava.io.tmpdir=${NGRINDER_HOME}/lib ${BASE_DIR}/ngrinder-*.war --port 80
