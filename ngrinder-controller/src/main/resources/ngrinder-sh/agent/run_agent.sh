#!/bin/sh
curpath=`dirname $0`
cd ${curpath}
while :
do
	if [ -d ./update_package/lib ]
	then
		echo UPDATE TO NEWER VERSION
		# update package and run
		rm -rf ./lib
		cp -rf ./update_package/* .
		rm -rf ./update_package
	fi
	./run_agent_internal.sh $@
	if [ ! -d ./update_package/lib ]
	then
		break
	fi
done