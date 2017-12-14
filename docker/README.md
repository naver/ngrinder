Introduction
==========

What is nGrinder?
---------

nGrinder is a platform for stress tests that enables you to execute script creation, test execution, monitoring, and result report generator simultaneously. The open-source nGrinder offers easy ways to conduct stress tests by eliminating inconveniences and providing integrated environments. It is licensed under the Apache License, Version 2.0.

> http://naver.github.io/ngrinder/

nGrinder consists of two major components. 

* __controller__ : a web application that enables the performance tester to create a test script and configure a test run. You can get the controller docker images [ngrinder/controller](https://registry.hub.docker.com/u/ngrinder/controller/).

* __agent__ : a virtual user generator that creates loads. You can get the agent docker images [ngrinder/agent](https://registry.hub.docker.com/u/ngrinder/agent/).

Version
---------
Current Version: 3.4

How to run nGrinder with dockers
===========================

Controller
------------ 
Install docker 1.5.0 or above  on your host.
Pull the ngrinder/controller image.

```
$ docker pull ngrinder-controller:3.4
```

Start controller.

```
docker run -d -v ~/ngrinder-controller:/opt/ngrinder-controller -p 80:80 -p 16001:16001 -p 12000-12009:12000-12009 ngrinder-controller:3.4
``` 

The controller creates a data folder under /opt/ngrinder-controller to maintain test history and configuration data. In order to keep the data persistently, you should map the folder /opt/ngrinder-controller on the container to a folder on your host . 

Port information:

* __80__: Default controller web UI port.

* __9010-9019__: agents connect to the controller cluster through these ports.

* __12000-12029__: controllers allocate stress tests through these ports.


Agent
--------
Install docker 1.5.0 or above on your another host. You should run your agent on different physical/virtual machine from the one where the controller is running since dockers running on the same machine cannot communicate each other without having to use an additional docker networking solution. In addition, agents might consume full resource on the machine to generate loads, so we strongly recommend to run nGrinder agent containers on the physically different machines from the one where controller is installed. 

Pull the ngrinder/agent image.

```
$ docker pull ngrinder/agent:3.4
```

Start agent.

```
docker run -v ~/ngrinder-agent:/opt/ngrinder-agent -d ngrinder/agent:3.4 controller_ip:controller_web_port
``` 
