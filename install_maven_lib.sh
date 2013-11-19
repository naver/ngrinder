#!/bin/sh
mvn install:install-file -Dfile=lib/native.jar -DgroupId=sigar -DartifactId=sigar-native -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=lib/grinder-3.9.1-patch.jar -DgroupId=grinder -DartifactId=grinder-patch -Dversion=3.9.1-patch -Dpackaging=jar
mvn install:install-file -Dfile=lib/universal-analytics-java-1.0.jar -DgroupId=org.ngrinder -DartifactId=universal-analytics-java -Dversion=1.0 -Dpackaging=jar