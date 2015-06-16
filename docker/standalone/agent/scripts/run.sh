#!/bin/bash

if [ -n "$CONTROLLER_ADDR" ]
then
	AGENT_URL="http://${CONTROLLER_ADDR}/agent/download"
	CNT=1
	cd ${BASE_DIR}

	while true
	do
		echo "trying to download agent from controller ${AGENT_URL}"
		wget -O ngrinder-agent.tar ${AGENT_URL}
		if [ $? -eq 0 ]; then
			tar -xvf ngrinder-agent.tar
			if [ -d "${BASE_DIR}/ngrinder-agent" ]; then
        			${BASE_DIR}/ngrinder-agent/run_agent.sh
				break
			else
				echo "Failed extracting ngrinder-agent.tar file"
			fi
		else
			echo "Failed downloading agent"
		fi
		
		sleep 5
	done
else
	echo "Please set CONTROLLER_ADDR environment variable"
fi

