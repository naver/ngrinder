#!/bin/bash

if [[ ! -f ~/.m2/repository/sigar/sigar-native/1.0/sigar-native-1.0.jar ]] ; then
	./mvnw install:install-file -Dfile=lib/sigar-native-1.0.jar -DgroupId=sigar -DartifactId=sigar-native -Dversion=1.0	-Dpackaging=jar -DcreateChecksum=true
fi

if [[ ! -f ~/.m2/repository/grinder/grinder-patch/3.9.1/grinder-patch-3.9.1.jar ]] ; then
	./mvnw install:install-file -Dfile=lib/grinder-3.9.1-patch.jar -DgroupId=grinder -DartifactId=grinder-patch -Dversion=3.9.1-patch -Dpackaging=jar -DcreateChecksum=true
fi

if [[ ! -f ~/.m2/repository/org/ngrinder/universal-analytics-java/1.0/universal-analytics-java-1.0.jar ]] ; then
	./mvnw install:install-file -Dfile=lib/universal-analytics-java-1.0.jar -DgroupId=org.ngrinder -DartifactId=universal-analytics-java -Dversion=1.0 -Dpackaging=jar -DcreateChecksum=true
fi
