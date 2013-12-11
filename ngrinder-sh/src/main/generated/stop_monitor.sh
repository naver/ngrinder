#!/bin/sh
curpath=`dirname $0`
cd ${curpath}
java -Dstart.mode=stopmonitor -server -cp "lib/*" org.ngrinder.NGrinderStarter
