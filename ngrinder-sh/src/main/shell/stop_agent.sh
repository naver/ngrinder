#!/bin/sh
curpath=`dirname $0`
cd ${curpath}
java -Dstart.mode=stopagent -server -cp "lib/*" org.ngrinder.NGrinderAgentStarter
