#!/bin/sh
curpath=`dirname $0`
cd ${curpath}
java -Dstart.mode=stopagent -jar ngrinder-core-${ngrinder.version}.jar -server
