#!/bin/bash

# Configuration
DEBUG_FALSE=0
DEBUG_TRUE=1
DEBUG_TYPE=$DEBUG_FALSE
SLEEP_TIME=500000
ETH_TYPE=eth0

KB=128 

if [ $DEBUG_TYPE == "1" ]; then
    echo " D) debug mode "
fi 

# Network Inbound / Outbound 

#before=`cat /proc/net/dev | grep $ETH_TYPE | cut -f2 -d: | awk '{print $1, $9}'`
#nbefore=(`echo $before | tr '.' ' '`)
#usleep $SLEEP_TIME
#after=`cat /proc/net/dev | grep $ETH_TYPE | cut -f2 -d: | awk '{print $1, $9}'`
#nafter=(`echo $after | tr '.' ' '`)

#inboundtemp=`expr ${nafter[0]} - ${nbefore[0]}`
#inbound=`expr $inboundtemp / $KB / $SLEEP_TIME`
#outboundtemp=`expr ${nafter[1]} - ${nbefore[1]}`
#outbound=`expr $outboundtemp / $KB / $SLEEP_TIME`

#if [ $DEBUG_TYPE == $DEBUG_TRUE ]; then
#        echo " D) sleep time : $SLEEP_TIME"
#        echo " D) inbound temp : $inboundtemp"
#        echo " D) outbound temp : $outboundtemp"
#fi

#echo $inbound,$outbound

# CPU value
#cat /proc/stat | grep -e '^cpu ' | grep -v grep | awk '{print $0}' > cpudata
ncpuinfo=`cat /proc/stat | grep -e '^cpu ' | grep -v grep | awk '{print $0}'`
cpuinfo=(`echo $ncpuinfo | tr '.' ' '`)
if [ $DEBUG_TYPE == $DEBUG_TRUE ]; then
        echo " D) cputime : ${cpuinfo[1]}, ${cpuinfo[2]}, ${cpuinfo[3]}, ${cpuinfo[4]}, ${cpuinfo[5]}"
fi

beforeCpuinfo1=${cpuinfo[1]}
beforeCpuinfo2=${cpuinfo[2]}
beforeCpuinfo3=${cpuinfo[3]}
beforeCpuinfo4=${cpuinfo[4]}
beforeCpuinfo5=${cpuinfo[5]}

usleep $SLEEP_TIME

ncpuinfo=`cat /proc/stat | grep -e '^cpu ' | grep -v grep | awk '{print $0}'`
cpuinfo=(`echo $ncpuinfo | tr '.' ' '`)

user=`expr ${cpuinfo[1]} - $beforeCpuinfo1`
system=`expr ${cpuinfo[2]} - $beforeCpuinfo2`
nice=`expr ${cpuinfo[3]} - $beforeCpuinfo3`
idle=`expr ${cpuinfo[4]} - $beforeCpuinfo4`
iowait=`expr ${cpuinfo[5]} - $beforeCpuinfo5 `
total=`expr $user + $system + $nice + $idle + $iowait`

echo $total, $idle

# Load Average
#cat /proc/loadavg  | awk '{print $1}'
nloadavg=`cat /proc/loadavg  | awk '{print $1,$2,$3}'`
loadavg=(`echo $nloadavg | tr ' ' ' '`)
echo ${loadavg[0]}, ${loadavg[1]}, ${loadavg[2]}

# IO read / write
#home=`df . | grep '/' | awk '{print $1}' | cut -d'/' -f3-`
#niostats=`iostat $home -d 1 1 | grep $home | awk '{print $3, $4}'`
#iostats=(`echo $niostats | tr '.' ' '`)
#echo ${iostats[0]}, ${iostats[2]}

# Free / Swap memory
totalMemory=`cat /proc/meminfo | grep MemTotal | awk '{print $2$3}'`
freeMemory=`cat /proc/meminfo | grep MemFree | awk '{print $2$3}'`
echo $totalMemory, $freeMemory