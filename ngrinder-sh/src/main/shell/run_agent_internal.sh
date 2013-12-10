#!/bin/sh
controllerIp=""
if [ $1 ]
then
  controllerIp="-Dcontroller=$1"
fi
curpath=`dirname $0`
java -Dstart.mode=agent ${controllerIp} -cp "lib/*" org.ngrinder.NGrinderStarter -server
