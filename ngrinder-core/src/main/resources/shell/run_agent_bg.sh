#!/bin/sh
curpath=`dirname $0`
nohup ${curpath}/run_agent.sh $1 >> /${TMPDIR:=/tmp}/ngrinder.log & 2>&1
