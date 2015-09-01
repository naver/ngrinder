#!/bin/bash

if [ -n "$CONTROLLER_ADDR" ]
then
	AGENT_URL="http://${CONTROLLER_ADDR}/agent/download"
	AGENT="${BASE_DIR}/ngrinder-agent"
	CNT=1
	cd ${BASE_DIR}
	echo "deleting pid..."
	rm -rf ~/.ngrinder_agent/pid
        if [ -d ${AGENT} ]; then
		echo "agent binary already exists."
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
	                CNT=$((CNT + 1))
	                echo $CNT
	                if [ $CNT -eq 3 ]; then
	                        break
	                fi
		done
	fi
else
	echo "CONTROLLE_ADDR environment varible is not set. Use the built in controller"
	AGENT="${BASE_DIR}/builtin/ngrinder-agent"
	cd ${AGENT}
	${AGENT}/run_agent.sh "$@"
fi

