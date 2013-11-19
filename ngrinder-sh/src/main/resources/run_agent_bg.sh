#!/bin/sh
curpath=`dirname $0`
nohup ${curpath}/run_agent.sh $1 > /dev/null & 2>&1
