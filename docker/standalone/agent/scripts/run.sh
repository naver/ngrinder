#!/bin/bash

if [ -n "$CONTROLLER_ADDR" ]
then
	AGENT_URL="http://${CONTROLLER_ADDR}/agent/download"
	AGENT="${BASE_DIR}/ngrinder-agent"
	cd ${BASE_DIR}

        if [ -d ${AGENT} ]; then
	        ${AGENT}/run_agent.sh
	else
		while true
		do
			echo "trying to download agent from controller ${AGENT_URL}"
			if [ -f "${AGENT}.tar" ]; then
				rm "${AGENT}.tar"
			fi
			wget -O ngrinder-agent.tar ${AGENT_URL}
			if [ $? -eq 0 ]; then
				tar -xvf ngrinder-agent.tar
				if [ -d "${AGENT}" ]; then
        				${AGENT}/run_agent.sh
					break
				else
					echo "Failed extracting ngrinder-agent.tar file"
				fi
			else
				echo "Failed downloading agent"
			fi
		
			sleep 5
		done
	fi
else
	echo "Please set CONTROLLER_ADDR environment variable"
fi

