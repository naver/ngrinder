#!/bin/sh
curpath=`dirname $0`
cd ${curpath}
while:
do
	if [ -f ./update_package ]
	then
		echo UPDATE TO NEWER VERSION
		# update package and run
		rm -rf .\*.jar
		cp -rf ./update_package/* .
		rm -rf ./update_package
	fi
	./run_agent_internal.sh $1
	if [ ! -f ./update_package ]
	then
		break
	fi
done