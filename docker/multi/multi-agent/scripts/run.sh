#!/bin/bash
if [ -n "$CONTROLLER_ADDR" ]; then
        if [ -n "$REGION" ]; then
                AGENT_URL="http://${CONTROLLER_ADDR}/agent/download?region=${REGION}"
                CNT=1
                cd ${BASE_DIR}

                while true; do
                        echo "trying to download agent from controller ${AGENT_URL}"
                        wget -O ngrinder-agent.tar ${AGENT_URL}
                        if [ $? -eq 0 ]; then
                                tar -xvf ngrinder-agent.tar
                                if [ -d "${BASE_DIR}/ngrinder-agent" ]; then
                                        IP_ADDR=$(echo $CONTROLLER_ADDR | cut -d ':' -f1)
                                        sed -i -e "s/\(agent.controller_host=\).*/\1$IP_ADDR/" ${BASE_DIR}/ngrinder-agent/__agent.conf
                                        ${BASE_DIR}/ngrinder-agent/run_agent.sh
                                        break
                                else
                                        echo "Failed extracting ngrinder-agent.tar file"
                                fi
                        else
                                echo "Failed downloading agent. Ensure parameters - controller address (CONTROLLER_ADDR) and region name (REGION)"
                        fi

                        sleep 5
                done
        else
                echo "Please set the name of region (REGION)"
        fi
else
        echo "Please set controller_ip_address:web_port (CONTROLLER_ADDR)"
fi

