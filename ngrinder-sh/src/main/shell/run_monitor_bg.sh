#!/bin/sh
curpath=`dirname $0`
nohup ${curpath}/run_monitor.sh $@ > /dev/null & 2>&1