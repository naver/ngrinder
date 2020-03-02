#!/bin/sh
os=`uname -a`
curpath=`pwd |awk -F '/' '{print $NF}'`
owned_user=`pwd |awk -F '/' '{print $NF}'|awk -F '-' '{print $NF}'`
if [ "$curpath" != "ngrinder-agent" ];then
    if ( echo ${os} |grep -q "inux" );then
        sed -i "s/.*agent.region=.*/agent.region=`echo NONE_owned_$owned_user`/" __agent.conf
        echo linux agent.region=$owned_user
    fi
    if ( echo ${os} |grep -q "Darwin" );then
         sed -i "" "s/.*agent.region=.*/agent.region=`echo NONE_owned_$owned_user`/" __agent.conf
         echo mac agent.region=$owned_user
     fi
    nohup ./run_agent.sh -o  -ah ~/.${curpath} --host-id ${curpath} $@ > /dev/null & 2>&1
else
    echo local
    curpath=`dirname $0`
    nohup ${curpath}/run_agent.sh -o $@ > /dev/null & 2>&1
fi
