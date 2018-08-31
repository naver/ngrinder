* nGrinder 3.4 has been released. See https://github.com/naver/ngrinder/releases

nGrinder 
========

[![Join the chat at https://gitter.im/naver/ngrinder](https://badges.gitter.im/naver/ngrinder.svg)](https://gitter.im/naver/ngrinder?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)


nGrinder is a platform for stress tests that enables you to execute script creation, test execution, monitoring, and result report generator simultaneously. The open-source nGrinder offers easy ways to conduct stress tests by eliminating inconveniences and providing integrated environments.


Want to know what's changed from the original grinder platform?
 * Checkout https://github.com/naver/ngrinder/wiki/Architecture !

To get to know what's different from previous ngrinder 2.0?
 * Checkout http://www.slideshare.net/junhoyoon3994/ngrinder-30-load-test-even-kids-can-do !

To get started,
 * Checkout https://github.com/naver/ngrinder/wiki/User-Guide !

You can find out what nGrinder looks like with screen-shot.
 * https://github.com/naver/ngrinder/wiki/Screen-Shot

nGrinder consists of two major components. 

nGrinder controller
 * a web application that enables the performance tester to create a test script and configure a test run

nGrinder agent
* a virtual user generator that creates loads.

Features
--------

* Use Jython script to create test scenario and generate stress in JVM using multiple agents.
* Extend tests with custom libraries(jar, py). It's unlimited practically.
* Provide web-based interface for project management, monitoring, result management and report management.
* Run multiple tests concurrently. Assign the pre-installed multiple agents to maximize each agent's utilization.
* Deploy agents on multiple network regions. Execute tests on various network locations
* Embed Subversion to manage scripts.
* Allow to monitor the state of agents generating stress and target machines receiving stress
* Proven solution which is used to test huge systems having more than 100 million users in NHN.


Download
--------

You can download the latest nGrinder in the following link. 
* https://github.com/naver/ngrinder/releases

Documentation
-------------
You can find the installation guide at the following link.
* https://github.com/naver/ngrinder/wiki/Installation-Guide

You can find the user guide at the following location link.
* https://github.com/naver/ngrinder/wiki/User-Guide



Contribution?
-------------
nGrinder welcomes any contributions from users. Please make all pull requests against master branches.
* Clone the REPO : 'git clone git://github.com/naver/ngrinder.git'

You can find general developer documents at the following link.
 * https://github.com/naver/ngrinder/wiki/Dev-Document

Versioning
----------

For transparency and insight into our release cycle, and to strive to maintain backward compatibility, Bootstrap will be maintained under the Semantic Versioning guidelines to the greatest extent possible.

Releases will be numbered in the following format:

      `<major>.<minor>.<patch>`

Release will be constructed based on the following guidelines:

* Breaking backward compatibility bumps the major (and resets the minor and patch)
* New additions without breaking backward compatibility bump the minor (and reset the patch)
* Bug fixes and small enhancement. changes bump the patch


Q/A and Bug tracker
-------------------
Found the apparent bug? Got a brilliant idea for an enhancement? Please create an issue here on GitHub so you can notify us!
* https://github.com/naver/ngrinder/issues

You can join our forum as well
* Dev : http://ngrinder.642.n7.nabble.com/ngrinder-dev-f1.html 
* User Forum : http://ngrinder.642.n7.nabble.com/ngrinder-user-f50.html
* 中文论坛 (Chinese) http://ngrinder.642.n7.nabble.com/ngrinder-user-cn-f114.html
* 한국어 유저 포럼 (Korean) http://ngrinder.642.n7.nabble.com/ngrinder-user-kr-f113.html
* [![Developer chat at https://gitter.im/naver/ngrinder-kr](https://badges.gitter.im/naver/ngrinder-kr.svg)](https://gitter.im/naver/ngrinder-kr?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

---------------------

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License. 
      
   
nGrinder includes the following software and libraries as follows. See the LICENSE folder for the license and copyright details for each.
* https://github.com/naver/ngrinder/tree/master/license
