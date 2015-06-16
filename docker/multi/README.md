Introduction
==========

What is nGrinder?
---------

nGrinder is a platform for stress tests that enables you to execute script creation, test execution, monitoring, and result report generator simultaneously. The open-source nGrinder offers easy ways to conduct stress tests by eliminating inconveniences and providing integrated environments. It is licensed under the Apache License, Version 2.0.

> [http://naver.github.io/ngrinder/](http://naver.github.io/ngrinder/)

nGrinder consists of two major components.

* __Controller__ : a web application that enables the performance tester to create a test script and configure a test run. You can get the controller docker images.

    Standalone version: [ngrinder/controller](https://registry.hub.docker.com/u/ngrinder/controller/)

    Easy clustering version: [ngrinder/multi-controller](https://registry.hub.docker.com/u/ngrinder/multi-controller/)

* __Agent__ : a virtual user generator that creates loads. You can get the agent docker images.

    Standalone verion: [ngrinder/agent](https://registry.hub.docker.com/u/ngrinder/agent/)

    Easy clustering verion: [ngrinder/multi-agent](https://registry.hub.docker.com/u/ngrinder/multi-agent/)

Version
---------
Current Version: 3.3


What is nGrinder easy clustering?
---------
From nGrinder 3.3, nGrinder supports "easy" clustering mode to minimize tedious environment setup tasks for your multi-region stress tests. Clustered multiple controllers run in a single machine and manage groups of agents which are distributed in many regions such as AWS regions, AZURE regions, your own DC or local machines, etc. through an unified Web UI.


You can find more information about nGrinder clustering.

+ [Cluster Architecture](http://www.cubrid.org/wiki_ngrinder/entry/cluster-architecture)

+ [Easy Clustering Guide](http://www.cubrid.org/wiki_ngrinder/entry/easy-clustering-guide)



How to run easy clustering nGrinder with dockers
===========================

Clustered Controller
------------

Install docker 1.5.0 or above  on your host.

Pull the ngrinder/multi-controller image.

```
$ docker pull ngrinder/multi-controller:3.3
```

Start multi-controller cluster.

```
docker run -d -v ~/.ngrinder:/root/.ngrinder \
    -e 'COUNT=number_of_regions' -e 'REGION_1=name_of_region' -e 'REGION_2=name_of_region' -e 'REGION_N=name_of_region'\
    -p 80:80 -p 9010-9019:9010-9019 -p 12000-12029:12000-12029 \
    ngrinder/multi-controller:3.3
```
The controller creates a data folder under /root/.ngrinder to maintain test history and configuration data. In order to keep the data persistently, you should map the folder /root/.ngrinder on the container to a folder on your host .


You should set the following configuration parameters.

* __COUNT__: the number of your regions (max N = 10)

* __REGION\_1 ~ REGION\_N__: the name of each region (default values = REGION\_1=region1 ~ REGION\_N=regionN)

Port information:

* __80__: Default controller web UI port.

* __9010-9019__: agents connect to the controller cluster thorugh these ports.

* __12000-12029__: controllers allocate stress tests through these ports.


For example, if you would like to conduct stress tests on your servers which are distributed to three regions - US, EU and ASIA, you can simply enter the following command on a machine to setup and start the ngrinder clustered controllers.

```
docker run -d -v ~/.ngrinder:/root/.ngrinder \
    -e 'COUNT=3' -e 'REGION_1=AWS_US' -e 'REGION_2=localDC_EU' -e 'REGION_3=AZURE_ASIA'\
    -p 80:80 -p 9010-9019:9010-9019 -p 12000-12029:12000-12029 ngrinder/multi-controller:3.3
```


Agent for multi-regions
--------
Install docker 1.5.0 or above on your another host.

You should run your agent on different physical/virtual machine from the one where the controller is running since dockers running on the same machine cannot communicate each other without having to use an additional docker networking solution. In addition, agents might consume full resource on the machine to generate loads, so we strongly recommend to run nGrinder agent containers on the physically different machines from the one where controller is installed.

Pull the ngrinder/multi-agent image.

```
$ docker pull ngrinder/multi-agent:3.3
```

Start agent.

```
docker run -d -e 'CONTROLLER_ADDR=controller_ip:controller_web_port' \
    -e 'REGION=the_name_of_region_to_which_this_agent_connects' \
    ngrinder/multi-agent:3.3
```
Default controller_web_port is 80.

For example, you have just installed clustered controllers above (REGION_1=AWS_US, REGION_2=localDC_EU, REGION_3=AZURE_ASIA). In case that IP address of the clustered controller is 111.222.333.444 and web port number is 80, you can simply enter the commands below on a machine of each region.

For AWS US region

```
docker run -d -e 'CONTROLLER_ADDR=111.222.333.444:80' -e 'REGION=AWS_US' ngrinder/multi-agent:3.3
```

For local DC region

```
docker run -d -e 'CONTROLLER_ADDR=111.222.333.444:80' -e 'REGION=localDC_EU' ngrinder/multi-agent:3.3
```

For AZURE ASIA region

```
docker run -d -e 'CONTROLLER_ADDR=111.222.333.444:80' -e 'REGION=AZURE_ASIA' ngrinder/multi-agent:3.3
```


Web UI
--------
All done!!

Now, you can create your own stress tests with the easy clustered nGrinder that you have just launched.

* URL: http://controller_ip:controller_web_port
* Default ID: admin
* Default PW: admin

Enjoy ~
