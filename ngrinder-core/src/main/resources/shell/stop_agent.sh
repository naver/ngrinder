#!/bin/sh
                  
AGENT_PID=`sed '/^\#/d' agent_pid.conf | grep 'agent.pid'  | tail -n 1 | cut -d "=" -f2-`

kill -9 $AGENT_PID

