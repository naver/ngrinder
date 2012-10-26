#!/bin/sh
curpath=`dirname $0`
cd ${curpath}
LD_LIBRARY_PATH="${curpath}/native_lib/:${LD_LIBRARY_PATH}"
export LD_LIBRARY_PATH
java  -Dstart.mode=monitor -jar ngrinder-core-${ngrinder.version}.jar -server