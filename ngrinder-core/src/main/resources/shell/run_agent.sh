#!/bin/sh
curpath=`dirname $0`
cd ${curpath}
controllerIp=""
if [ $1 ]
then
  controllerIp="-Dcontroller=$1"
fi
LD_LIBRARY_PATH="${curpath}/native_lib/:${LD_LIBRARY_PATH}"
export LD_LIBRARY_PATH
java -Dstart.mode=agent ${controllerIp} -jar ngrinder-core-${ngrinder.version}.jar -server
