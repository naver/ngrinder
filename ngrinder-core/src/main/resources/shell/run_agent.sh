#!/bin/sh
curpath=`dirname $0`
cd ${curpath}
java -Dstart.mode=agent -jar ngrinder-core-${ngrinder.version}.jar -server