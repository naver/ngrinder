#!/bin/sh
curpath=`dirname $0`
cd ${curpath}
controllerIp=""
if [ $1 ]
then
  controllerIp="-Dcontroller=$1"
fi
java -Dstart.mode=agent ${controllerIp} -jar ngrinder-core-${ngrinder.version}.jar -server
