#!/bin/sh
curpath=`dirname $0`
cd ${curpath}
java -server -cp "lib/*" org.ngrinder.NGrinderAgentStarter --mode=monitor --command=stop $@
