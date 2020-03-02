#!/bin/sh
curpath=`dirname $0`
os=`uname -a`

if ( echo ${os} |grep -q "inux" );then
    local_ip=`/sbin/ifconfig eth0|grep 'inet addr' |awk '{print $2}'|awk -F ":" '{print $2}'`
    if [ -z "$local_ip" ];then
        local_ip=`/sbin/ifconfig eth0|grep 'inet' |awk '{print $2}'`
    fi
    sed -i "s/.*monitor.binding_ip=.*/monitor.binding_ip=`echo $local_ip`/" __agent.conf

    echo linux replace ip: $local_ip
elif ( echo ${os} |grep -q "Darwin" );then
    local_ip=`/sbin/ifconfig en0|grep cast|awk '{print $2}'|awk -F ":" '{print $1}'`
    sed -i "" "s/.*monitor.binding_ip=.*/monitor.binding_ip=`echo $local_ip`/" __agent.conf
    echo mac replace ip: $local_ip
fi
nohup ${curpath}/run_monitor.sh -o $@ > /dev/null & 2>&1

