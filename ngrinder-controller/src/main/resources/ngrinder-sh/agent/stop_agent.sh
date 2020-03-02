#!/bin/sh
curpath=`pwd |awk -F '/' '{print $NF}'`
if [ "$curpath" != "ngrinder-agent" ];then
    agent_pid=`ps -ef | grep 'org.ngrinder.NGrinderAgentStarter' | grep 'mode=agent' | grep -v grep | grep $curpath | awk '{print $2}'`
    echo kill agent pid: $agent_pid
    kill -9 $agent_pid
    echo kill agent success!
else
    curpath=`dirname $0`
    cd ${curpath}
    java -server -cp "lib/*" org.ngrinder.NGrinderAgentStarter  --mode=agent --command=stop $@
fi
