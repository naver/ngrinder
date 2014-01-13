#!/bin/sh
curpath=`dirname $0`
nohup ${curpath}/run_agent.sh $@ > /dev/null & 2>&1
