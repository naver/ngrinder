#!/usr/bin/env bash
controller=$1
shift
if [ ! -n "$controller" ]
then
    echo "controller:port should be provided as an argument to download an agent"
    controller="controller:80"
    echo "use controller:80 as a default"    
fi
AGENT_DOWNLOAD_URL="http://$controller/agent/download"
cd $BASE_DIR
echo "deleting pid..."
rm -rf $NGRINDER_AGENT_HOME/pid
if [ -e "$NGRINDER_AGENT_BASE/run_agent.sh" ]; then
    echo "agent binary already exists."
else
    for i in {1..30};
    do
        wget -O ngrinder-agent.tar -T 60 $AGENT_DOWNLOAD_URL && break || sleep 10;
    done
    if [ ! -f "$BASE_DIR/ngrinder-agent.tar" ];
    then
       echo "fail to download an agent file from " $AGENT_DOWNLOAD_URL
       exit 1
    fi
    tar -xvf ngrinder-agent.tar
fi

$NGRINDER_AGENT_BASE/run_agent.sh $*
