

nGrinder 
========

It is the platform of stress tests which enables you to execute script creation, test execution, monitoring, and result report generator simultaneously. The opensource nGrinder offers easy ways to conduct stress tests by eliminating inconveniences and providing integrated environments.

To get started, checkout http://www.nhnopensource.org/ngrinder_document!

nGrinder consists of two major components. 

* nGrinder controller is a web application which let performance tester create test script and configure test run
* nGrinder agent is a virtual user generator which makes loads. 

Currently nGrinder depends on CUBRID(http://www.cubrid.org) for database layer. 

* nGrinder is now being developed. It'll be released around October, 2012.
 
Features
--------

* Use Jython script to create scenario and generating stress in JVM. 
* Provide web-based interface for project management, monitoring, result management and report management. 
* Enable to monitor the state of agents generating stress and target machines receiving stress
* Enable to inquire past test results by saving test history and provide absolute link to share with those who do not have private account. 
* Enable to converse image and csv from original results. 

Quick Start
-----------
* You can find out usage videos in the following location
 * http://www.youtube.com/user/ngrinderdev

* You can also find the installation guide in the following.
 * http://www.cubrid.org/wiki_ngrinder

* How to clone the repo 
 * 'git clone git://github.com/nhnopensource/ngrinder.git'
 
Versioning
----------

For transparency and insight into our release cycle, and for striving to maintain backward compatibility, Bootstrap will be maintained under the Semantic Versioning guidelines as much as possible.

Releases will be numbered with the follow format:

`<major>.<minor>.<patch>`

And constructed with the following guidelines:

* Breaking backward compatibility bumps the major (and resets the minor and patch)
* New additions without breaking backward compatibility bumps the minor (and resets the patch)
* Bug fixes and misc changes bumps the patch


Bug tracker
-----------

Have a bug or enhancement idea? Please create an issue here on GitHub if you want just notify to us! 

https://github.com/nhnopensource/ngrinder/issues

Our official bug tracker is following. You can monitor our development status.

http://jira.cubrid.org/browse/NGRINDER


Contributing
------------

Please make all pull requests against master branches.


Copyright and license
---------------------

BDS License 

nGrinder (http://www.cubrid.org/wiki_ngrinder)

Copyright (c) 2011 NHN Business Platform
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
    * Neither the names of the copyright holders nor the names of the
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
DAMAGE.


nGrinder includes the softwares and libraries as follows. 
See the folder LICENCE for license and copyright details for each.

- Grinder
- jQuery
- jUnit
- iBatis
- jfreechart
- Apache Commons
- JSON simple
- Apache log4j
- EditArea
- JOFC2