#!/bin/sh
curpath=`dirname $0`
cd ${curpath}
java -Dstart.mode=monitor -server -cp "lib/*" org.ngrinder.NGrinderStarter