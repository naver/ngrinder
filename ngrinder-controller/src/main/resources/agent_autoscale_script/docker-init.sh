#!/bin/bash
set -e -x
yum install wget -y
# increase file open limit
echo "root soft  nofile 40000" >> /etc/security/limits
echo "root hard  nofile 40000" >> /etc/security/limits
ulimit -n 40000
sed -i s/'Defaults    requiretty'/'#Defaults    requiretty'/ /etc/sudoers                  # allow root access without tty.
wget -qO- https://get.docker.com/ | sh >> /tmp/ngrinder_agent_provision.log 2>&1           # install docker
#Below line is for Amazon generic unix system, if the VM is based on Ubuntu, should check /etc/default/docker DOCKER_OPTS
sed -i s/'^OPTIONS='/'OPTIONS="-H tcp:\/\/0.0.0.0:10000 -H unix:\/\/\/var\/run\/docker.sock"'/ /etc/sysconfig/docker           # allow tcp access
chkconfig docker on																		   # make docker auto startable
service docker start                                                                       # start docker
docker pull ngrinder/agent:3.3-p1                                                             # download docker image