#!/bin/sh
                  
MONITOR_PID=`sed '/^\#/d' agent_pid.conf | grep 'monitor.pid'  | tail -n 1 | cut -d "=" -f2-`

kill -9 $MONITOR_PID

