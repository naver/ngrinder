3.4.2 (2018.08.20)
==================

> This is the last version in 3.4.X. 
  nGrinder supports only JDK1.7 and JDK1.8.  JDK1.9 or above will be supported from 3.5.

- New feature and changes
  * Speed up the page navigation when there are many users who have the followers.
    - Use LAZY patching in JPA
    - Use Hibernate 2nd Level Cache not to query user
    - Use id for perftest ordering instead of modified_date to avoid full db search
  * Add light security mode
    - Allows multicast / tcp connection to unspecified address compared normal security mode.
    - Can be configured by adding ```controller.security.level=light``` in system.conf 
  * Provide the way to turn off security mode in agent config
    - Allow the some agent to ignore security mode restriction
    - Can be configured by adding ```agent.enable_security=false``` in agent.conf     
  * Make agent list shown even for not admin user.
    - Lists public and own agents in the agent management menu even for non admin users.

- Bug Fix
  * #274 Use internal in monitor and report plugin
  * #278 fix bug when har file has no params
  * #294 Fix bandWidth calculation
  * #295 Fix csv separator inside SingleConsole
  * #297 Remove context path from AjaxObj url
  * #299 Modify NGrinderSecurityManager read access
  * #303 Fix failed controller test case
  * #308 Fix test failure
  * #318 Fix invalid reference bug in login.ftl
  * #323 Fix invalid rampup section layout
  * #335 Make GTest only instrument method in ngrinder context
  
- Improvement
  * #291 Speed up page navigation
  * #293 Add setting security level in system configuration
  * #317 Show agent list to non admin user
  * #330 Provide the way to turn off security mode in agent config
  
3.4 (2016.05.24)
================


> We have not been publically released nGrinder for last 2 years. It's true that we thought previous nGrinder was enough for our internal use.
As time goes by, surrounding circumstances(for example Java version or Docker) was been changing.. So we decided to continuously enhance nGrinder.


- New features and changes
  * Support Java 1.8
    - nGrinder now supports Java 1.8. Controller supports java 1.7 or above and Agent supports java 1.6 or above.
  * Provide elaborated script generator
    - Since nGrinder 3.0, we provided the script generation wizard that helps users easily create a default script.
    -  However, it only created simple scripts for GET method case and people frequently asked to us how to use the other methods, how to use cookies and headers in the script.
    - In nGrinder 3.4, you can choose one of methods such as GET, POST and set HTTP headers and cookies in script creation form, then it will create useful example codes for you.
  * Provide a brand new nGrinder script recorder
    - A few years ago, we had released the script recorder based on JXBrowser. However it was not easy to run it due to several reasons.
    -  Now, we have developed a brand-new script recorder from the scratch using the Chrome extension technology. If you are using Chrome browser, you can install nGrinder Script Recorder package.
  * Provide the dockerized nGrinder
    - Now, dockerize is becoming as one of fashions now. You can easily run dockerized nGrinder.
    - See https://hub.docker.com/r/ngrinder/controller/
  * Introduce new plugin architecture based on the PF4J
    - Previously we have used Atlassian Plugin Framework(APF) for plugins. However we found out it was too overwhelmed way to ngrinder extension and also found there are not much developers familiar with it.
    - So we dropped APF and adopt PF4J instead. See https://github.com/naver/ngrinder/wiki/How-to-develop-plugin for details.
  * Add intro.js for easy understanding.
    - You can find the very small button "Tip" on every pages. Click it and see what it is.

- Bug Fix
  * #136 Support CJK for folder and script name
  * #111 GrinderUtils.getThreadUniqId() is not working in IntelliJ context
  * #103 Fix security issue
  * #104 TPS is the counting double


- Improvement
  * #73 Configurable CSV file format
  * #91 Support Java 1.8
  * #158 Make vuser count optional in REST API
  * #147 Move ngrinder official link to naver.github.io
  * #115 Provide the script sample link in "create script modal"


3.3.1 (2015.06.31/Internal Release)
===================================

- Bug Fix
  * #32 Abnormal execution result if the script has a null character(0x0).
  * #33 Too fast timeout when connecting Mbean.
  * #40 Cannot switch an user who has 2 characters name on switching other user.
  * #41 Some mac os didn't show "Add Host" button well on the script editor.
  * #43 Monitor is not accessible when IP6 is not supported.
  * #44 The time differences b/w controller and agents causes agent.conf overwriting.
  * #45 JDK7 prior to JDK7u50 in windows does not accept the classpath "lib/*".
  * #46 Fix wrong chart interval on the monitor page.
  * #47 Remove unnecessarily verbose log in controller.


- Improvement
  * #30 Get the safe file transmission size as long type.
  * #34 Provide intellij profile in groovy maven project.
  * #36 Provide the option which disables the local DNS.
  * #37 Make the agent work with JRE under security mode.
  * #38 Need to allow changing a timeout which is related while @BeforeProcess is running.


- Trivial Fix
  * Make the target host field 65535 varchar.
  * Modify popover function for showing "Test Log"
  * Fix a new line issue of install_maven_lib.sh on unix platform.


3.3 (2014.02.12)
================

> nGrinder 3.3 is the major release including several new features.
You should upgrade both agent and controller to 3.3 version to enjoy the new features.

- New feature and changes
  * One single executable package
    - Admins can run nGrinder controller using the following command.
      "java -XX:MaxPermGen:200m -jar ngrinder-controller-X.X.war"
    -  nGrinder controller war file became the self-executable and Tomcat mount is supported as well.
    - Admins can download agents and monitors from the controller and
      users can download private agents and monitors from the controller.
    -  The downloaded agent/monitor packages already contains the pre-defined configuration to connect the controller from which they are downloaded,
      so that no agent configuration is necessary.
    - See http://www.cubrid.org/wiki_ngrinder/entry/installation-guide
  * No more controller-agent connection problem
    - Previously nGrinder controller was bound to only one IP among all available IPs in the single machine. Sometimes this made it difficult to run nGrinder in which the internal and external IP are separated.(such as EC2)
    - Now nGrinder controller is bound to all available IPs in the OS to listen all connection requests such as agent connection, cluster member discovery, web connections so that the less connection error will be
      occurred.
    - Even IP6 are fully supported.
  * Easier command line options
    - The controller and agents now have the detailed command line interface.
    - Please run with -h when executing them to see the available options.
    - Even multiple agent and controller executions in a single machine have become very easy.
    - See http://www.cubrid.org/wiki_ngrinder/entry/controller-configuration-guide#command_line_options
    - and http://www.cubrid.org/wiki_ngrinder/entry/agent-configuration-guide#command_line_options
  * Agent automatic update
    - Now agents can be upgradable by clicking "Update" button in the controller's agent management page.
    - See http://www.cubrid.org/wiki_ngrinder/entry/agent-auto-update
  * Very small monitor package(<5MB)
  * Thread Ramp-Up
    - The underlying "The Grinder" engine supports only per process ramp up, but not thread ramp up.
    - nGrinder has enhanced The Grinder engine to support the thread ramp up as well
      so that more smooth ramp up can be performed.
    - See http://www.cubrid.org/wiki_ngrinder/entry/how-to-ramp-up-by-threads
  * Full REST APIs.
    - A user and admin can call the REST APIs to manage tests and scripts.
    - The REST APIs includes user and agent management API as well for the admin user.
    - See http://www.cubrid.org/wiki_ngrinder/entry/rest-api
  * L4 simulation by custom DNS rotation.
    - A user can map multiple IPs into a single DNS name in the target host configuration so that Whenever the DNS name and ip resolution is requested, the agent resolves DNS name into the random IP.
    - Check out http://www.cubrid.org/wiki_ngrinder/entry/how-to-change-dns#l4_simulation
  * SVN 1.8 Support and user switch support
    - Underlying SVNKit has been upgraded to 1.8.3 version so that 1.8 SVN client works well
    - Fix the access deny issue when followers access the other's SVN.
  * More consistent configuration keys.
    - Configuration keys used in the previous version are supported as well.
    - However we recommends to use the new system configuration keys.
    - You can see the configuration key mapping table in the followings.
      * http://www.cubrid.org/wiki_ngrinder/entry/controller-configuration-guide#controller_configuration_keys
      * http://www.cubrid.org/wiki_ngrinder/entry/agent-configuration-guide#agent_configuration_keys
  * The user self sign-up
    - The new user can sign up by himself/herself. The admin doesn't need to care about the user registration anymore.
    - See http://www.cubrid.org/wiki_ngrinder/entry/user-sign-up
  * Easier default configuration
    - By default, Agents are now automatically approved when they connect to the controller.
    - By default, Script console is disabled by default to reduce the unnecessary complexity.
    - By default, Email and Mobile phone number fields in the user creation page is not mandatory.
  * More consistent plugin interfaces
    - All plugins should be updated to the latest version which support nGrinder 3.3.
  * Groovy as a the first-citizen.
    - Groovy was upgraded to 2.2.1 version abd became the first citizen of nGrinder.
  * Custom test report plugins support
    - Some plugins will be provided soon such as JVMMonitor.
  * In addition, several UI improvements.


- Bug Fix
  * [NGRINDER-679] - Support intellij
  * [NGRINDER-680] - Make test parameter removable
  * [NGRINDER-681] - Fix NullPointerException while getting monitor data
  * [NGRINDER-684] - Fix typo errors
  * [NGRINDER-694] - Make unit tests work again
  * [NGRINDER-695] - Make the Initial process recognized in ramp up settings
  * [NGRINDER-700] - ; in URL should be changed to _ when creating an script
  * [NGRINDER-705] - Fix user deletion failure
  * [NGRINDER-706] - Fix the agent resolution bug with regions having same prefix
  * [NGRINDER-707] - Provide the attached agent list in healthcheck messages
  * [NGRINDER-714] - Clean up intellij warning
  * [NGRINDER-715] - Fix the incorrect svn author property bug when editing files in controller
  * [NGRINDER-716] - Make Asian languages correctly shown in the agent log
  * [NGRINDER-719] - Fix unrecognized user svn path error when creating a user with not trimmed user id
  * [NGRINDER-723] - Cache expiration is necessary when the user's followers are changed.
  * [NGRINDER-724] - SVN access from followers is denied.


- Improvement
  * [NGRINDER-289] - Test Compare feature should be provided
  * [NGRINDER-486] - Support agent auto update
  * [NGRINDER-675] - Add statistics data chart at perftest list
  * [NGRINDER-676] - Provide more consistent plugin interfaces
  * [NGRINDER-677] - Fix checkstyle errors
  * [NGRINDER-682] - Adjust logger level to see only necessary logs
  * [NGRINDER-685] - Provide custom report plugin structure
  * [NGRINDER-686] - Delete all jnlp related codes from ngrinder
  * [NGRINDER-687] - Make OnTestSamplingRunnable initiated whenever the test is executed.
  * [NGRINDER-689] - Clearfy index page logic
  * [NGRINDER-691] - Refactor ngrinder to be readable
  * [NGRINDER-692] - Support IPv6 and bind to all IPs
  * [NGRINDER-693] - Add agent version info agent list page
  * [NGRINDER-696] - Provide dynamic agent status view in the agent list
  * [NGRINDER-697] - Use freemarker macro to make ftl more concise
  * [NGRINDER-698] - Switch from Jython to Groovy in ScriptConsole
  * [NGRINDER-699] - Make APIs and maven project name consistent
  * [NGRINDER-701] - Make monitoring service clean
  * [NGRINDER-702] - Review all java methods doc
  * [NGRINDER-703] - Make the configuration keys more consistent
  * [NGRINDER-704] - Make the script syntax check optional during script validation
  * [NGRINDER-708] - Remove unnecessary replicated cache
  * [NGRINDER-710] - Add the easy cluster mode using local file system and h2 tcpserver
  * [NGRINDER-711] - Make the default configuration easily manageable
  * [NGRINDER-712] - Make ngrinder war executable from command line
  * [NGRINDER-713] - Reduce unnecessary db and access to speed up
  * [NGRINDER-717] - Make the unused region's agent info deletable.
  * [NGRINDER-720] - Make not frequently used configurations collapsible
  * [NGRINDER-721] - Provide easy user switch for admin
  * [NGRINDER-722] - Support SVN 1.8


- New Feature
  * [NGRINDER-678] - Allow user sign up
  * [NGRINDER-688] - Allow multiple DNS entries to simulate the L4.
  * [NGRINDER-690] - Provide full REST APIs
  * [NGRINDER-709] - Make admin password be able to reset
  * [NGRINDER-718] - Enable ramp up by thread


3.2.3 (2013.10.04)
==================

> nGrinder 3.2.3 is the bug fix and minor enhancement release.
Special Thanks to Karel Piwko and Egoing.

- New feature and changes
  * Basic web API is supported. A user can call the APIs to clone and start the test using HTTP client such as curl. This is protected by HTTP Basic Auth.
    http://www.cubrid.org/wiki_ngrinder/entry/controller-api
  * Parameterized test is supported. Each test can get a parameter in the test configuration page and scripts can refer it to control its actions.
    http://www.cubrid.org/wiki_ngrinder/entry/how-to-pass-a-parameter-to-the-script
  * Per-Test statistics are enabled for mean test time and errors as well.
  * Console port is now configurable so that you can use the opened ports if the network firewall doesn't block them.
  * And several UI improvements are included.


- Bug Fix
  * [NGRINDER-659] - Fix typo errors
  * [NGRINDER-668] - Fix wrong test schedule calculation when browser timezone and user specified timezone are different


- Improvement
  * [NGRINDER-654] - Make total agent count accessible from the script
  * [NGRINDER-655] - Make validation timeout configurable
  * [NGRINDER-656] - Provide more sampling intervals
  * [NGRINDER-658] - Add more clear test / errors
  * [NGRINDER-662] - Add more per-test statistics
  * [NGRINDER-663] - Show the warning message when a user overwrites the more recent script
  * [NGRINDER-666] - Sanitize logs
  * [NGRINDER-667] - Import NVPair in the template as default
  * [NGRINDER-670] - Show the pertest owner in the perftest detail page
  * [NGRINDER-671] - Elaborate javadoc
  * [NGRINDER-672] - Make the agent control server port configurable in the controller
  * [NGRINDER-673] - Make user id allow . (dot) in the middle of id.
  * [NGRINDER-674] - Show JAVA_HOME env var absent message when running agent as security mode


- New Feature
  * [NGRINDER-660] - Enable parameterized test
  * [NGRINDER-661] - Provide a convenient utility class to retrieve the test execution info
  * [NGRINDER-665] - Add basic web api support


3.2.2 (2013.08.29)
==================

> nGrinder 3.2.2 is the bug fix and minor enhancement release.

- New feature and changes
  * Since 3.2, the SVN has not been protected by ID/PW scheme by mistake. It's a very serious security problem. Therefore 3.2.2 adds the HTTP Basic protection for SVN access.
  * Now users can user sub folders under the lib and resource folder. They will be transferred to the agents as well.
  * The admin and normal users can configure the additional JAVA command line options for the agent process by providing agent.javaopt in the agent.conf
    and grinder.jvm.arguments in grinder.properties file at the same folder where the script exists respectively. This is for the extreme use case of ngrinder.
  * And several UI improvements are included.

- Bug Fix
  * [NGRINDER-633] - Make agent to connect the real ip first if no agent.controller.ip is provided
  * [NGRINDER-634] - Test description including ' breaks the test list layout
  * [NGRINDER-640] - &para turns into different symbol in the code editor
  * [NGRINDER-641] - Fix the popover width overflow when the script path is long
  * [NGRINDER-643] - Use ${NGRINDER_HOME}/subversion folder for subversion auth store
  * [NGRINDER-644] - SVN access doesn't block the illegal access
  * [NGRINDER-648] - User password is changed whenever the user is logined with login plugin

- Improvement
  * [NGRINDER-631] - Make monitor be able to select listening IP
  * [NGRINDER-632] - Make perftest distribute sub folders in the resource / lib folder
  * [NGRINDER-635] - Show the error rate rather than error count
  * [NGRINDER-636] - Add running test view filter in the test list
  * [NGRINDER-638] - Add the new announcement labels to show announcement change explicitly
  * [NGRINDER-639] - Provide the flexible way to customize the java opt when invoking agent process
  * [NGRINDER-642] - Check the java version during agent starting
  * [NGRINDER-645] - Add ${NGRINDER_HOME}/logback.xml recognized by ngrinder to customize logger
  * [NGRINDER-647] - Make ngrinder.controller.ip as well as ngrinder.controller.ipaddress recognizable.
  * [NGRINDER-649] - Groovy Maven Project should support src/main/groovy directory
  * [NGRINDER-651] - Add the test execution hook in javascript to validate the configuration by admin.
  * [NGRINDER-652] - Add the way for javascript to determine the current language

3.2.1 (2013.06.10)
==================

> nGrinder 3.2.1 is the bug fix and minor enhancement release.
We applied nGrinder 3.2 internally for 1 month and found several enhancement point.
This made this version more concrete and workable in the large deployment.

- New feature and change
  * Almost all of HTML pages are rewritten to follow the HTML standard. Now it supports IE10 perfectly.
  * Now you can see the scripting error in the agent log to easily find the scripting problem.
  * Groovy Engine becomes more concrete. Now each thread use its own JUnit Runner object so that each test is thread safe.
    - In addition, @RunRate annotation for each test method is available to control the run frequency.
    - It loads Groovy script with UTF-8 encoding as well so that a user can write the CJK characters.
  * The available agent memory is calculated more accurately. Instead of using free memory, now it uses actual free memory.
  * Each performance test page retrieval becomes speedy even when there are many files in SVN.

- Bug Fix
  * [NGRINDER-600] - Fix IE10 compatibility issue
  * [NGRINDER-608] - Make the memory setting more concrete for test process
  * [NGRINDER-610] - Fix typo errors
  * [NGRINDER-612] - Make controller's IP configurable
  * [NGRINDER-613] - Make script error transferred from agent
  * [NGRINDER-615] - Make startime null when test is scheduled
  * [NGRINDER-616] - Make monitoring data failure concrete
  * [NGRINDER-617] - Fix wrong classpath filtering in the process execution
  * [NGRINDER-618] - Dynamic system configuration update is not working after the first update.
  * [NGRINDER-622] - Add host settings for groovy maven project  in quicktest
  * [NGRINDER-625] - Make groovy tc thread use different test object
  * [NGRINDER-629] - Clean up the url after upload files
  * [NGRINDER-630] - Use UTF-8 for groovy class loading


- Improvement
  * [NGRINDER-609] - Speed up the perf test detail page view
  * [NGRINDER-611] - Clean up ftl files
  * [NGRINDER-619] - Make the easier method name filtering for record
  * [NGRINDER-620] - Increase the default max vuser to 2000
  * [NGRINDER-621] - Elaborate script template
  * [NGRINDER-623] - Shrink the script editor when expanding validation result panel
  * [NGRINDER-624] - Enable runNumber in groovy script in JUnitContext
  * [NGRINDER-627] - Make groovy runner efficient
  * [NGRINDER-628] - Enable the package folder structure for groovy maven project


- New Feature
  * [NGRINDER-626] - Add @RunRate annotation to control the run frequency for each @Test method



3.2 (2013.05.10)
================

> nGrinder is almost approaching to the ultimate performance testing solution.
We believe nGrinder 3.2 is the most developer friendly performance testing tool ever.

- New feature and change
  * We support not only Jython as a script language but also JUnit styled Groovy and Groovy Maven Project from 3.2.
    - It is the amazing approach in which you can easily pre-run and debug the test in your IDE such as eclipse connecting to nGrinder subversion repository.
    - Check out http://www.cubrid.org/wiki_ngrinder/entry/groovy-script
  * We support more vusers per agent by calibrating agent's JVM options.
    - nGrinder 3.2 supports 4000 vuser(8 times more than before) per agent when you use Groovy and 4GRam agents.
    - Delete ${NGRINDER_HOME}/process_and_thread_policy.js to apply new process and thread policy in prior to ngrinder 3.2 installation.
  * Now agent machines are rarely crashed by memory swapping. nGrinder controller stops the test which uses more than 97% memory of agents by force.
  * IE10 is now partially supported. We added IE9 compatibility mode option to show nGrinder correctly in IE10. We will continue to completely fix this by 3.2.1
  * Graph fluctuation by nGrinder internal processing overhead is reduced. Now you can see the more stable graph.
  * The nGrinder usage is reported to our Google analytics everyday. We only collect the controller IP and the count of performance test run the last day.
    - If you don't like it, please turn it off by setting usage.report=false in system.conf
  * A user can run agents with the more detailed memory options
    - by providing grinder.memory.permsize=Value and grinder.memory.maxpermsize=Value in the unit of mega in grinder.properties file as same folder of the selected script.
  * SVN user can enjoy the user switching feature as well just like you do in the browser.
  * And.. several minor UI enhancements.


- Bug Fix
  * [NGRINDER-414] - Fix access error to the shared user's repo by SVN
  * [NGRINDER-449] - Fix script display when 2 admin users have a script with same name
  * [NGRINDER-580] - Fix the graph fluctuation
  * [NGRINDER-584] - Make agent number as 0 in script validation phase
  * [NGRINDER-587] - Fix PermGen error when running a big script
  * [NGRINDER-589] - Exclude please_modify_this.com in the target host string
  * [NGRINDER-599] - Fix column size overflow for very many tests
  * [NGRINDER-600] - Fix IE10 compatibility in which modal dialog box is not shown


- Improvement
  * [NGRINDER-85] - Make target monitor port is configurable.
  * [NGRINDER-431] - Make cluster mode configuration more stable
  * [NGRINDER-582] - Make duration updated after the test is finished
  * [NGRINDER-585] - Fix to return active page when agent is approved or disapprove.
  * [NGRINDER-586] - Forward nohub log to /dev/null
  * [NGRINDER-590] - Report the ngrinder usage to google analytics
  * [NGRINDER-593] - Make long script path visible
  * [NGRINDER-597] - Make the user password unchangeable in demo mode
  * [NGRINDER-598] - Add svn URL breadcrumb
  * [NGRINDER-601] - Make the validation result panel expandable
  * [NGRINDER-602] - Show the user id in perftest list
  * [NGRINDER-603] - Make admin see the lib and resources of other users.
  * [NGRINDER-604] - Make mobile phone number and email fields in user profile not mandatory
  * [NGRINDER-605] - Increase vuser limit
  * [NGRINDER-606] - Make agent machine never hang or die


- New Feature
  * [NGRINDER-476] - Add realtime monitor review page
  * [NGRINDER-561] - Add generic JNLP download feature for recorder and agent
  * [NGRINDER-591] - Add groovy support
  * [NGRINDER-594] - Add groovy maven project


3.1.3 (2013.03.29)
==================

> We're proudly releasing nGrinder 3.1.3 which fixes bugs and visibility.
This is the last version 3.1.X series, we'll work on 3.2 as a next release.

- New features and changes:
  * Now test report can be viewed without login.
  * A user can specify the sampling interval to make the graph not to be fluctuated.
  * A user can see the per test TPS chart when the sampling interval is greater than 2.
  * A user can upload and use native libs (so, dll) t o reuse existing assets. This is enabled only if the security mode is disabled.
  * A user can provide the any kind of custom value for monitor by regularly outputting the GC count and JVM memory usages.
  * A user can upload dll or so to run test using native libs. SecurityMode should be disabled and JNA should be imported for native lib access.


- Bug Fix
  * [NGRINDER-560] - Make test report visible without login
  * [NGRINDER-564] - Ignore Sigar Exception while collecting network usage.
  * [NGRINDER-569] - Make agent status string longer
  * [NGRINDER-570] - Make ignore sampling option affect to the total statistics
  * [NGRINDER-571] - Fix DB errors occurred during running test because agent_status field is not long enough
  * [NGRINDER-572] - Fix inconsistency b/w total and per test statistics by timing difference
  * [NGRINDER-573] - Make cookie parsing impl work with secure=1 entry
  * [NGRINDER-575] - Make graph start from 0 on x axis
  * [NGRINDER-576] - Make nGrinder compilable with missing jars
  * [NGRINDER-578] - Make smooth sampling values for consequent calls for catching up the time


- Improvement
  * [NGRINDER-243] - Provide an export image button for each chart
  * [NGRINDER-554] - Provide user defined monitoring feature
  * [NGRINDER-555] - Provide network usage graph in report page
  * [NGRINDER-556] - Save memory for monitoring graph display
  * [NGRINDER-557] - Provide the custom monitoring guide
  * [NGRINDER-559] - Improve monitor chart performance
  * [NGRINDER-562] - Provide per-test TPS graph in the detail report page
  * [NGRINDER-563] - Update jqPlot to new version.
  * [NGRINDER-565] - Provide the customizable agent log
  * [NGRINDER-566] - Make validation service accessible from plugin
  * [NGRINDER-574] - Introduce the sampling interval
  * [NGRINDER-577] - Provide the way to reserve the agent memory for native libraries.
  * [NGRINDER-579] - Add jna support


3.1.2 (2013.02.22)
==================

> We're proudly releasing nGrinder 3.1.2 which enhances the usability and visibility.
With some bug fix, the following improvements are for the effective operations of nGrinder.

- New features and changes:
  * Enable realTime target server monitoring. Now you can see how the target server reacts during tests.
  * Monitor agent network usages and be able to run the plugin to do something on the agents.
  * See logs passed from agents  directly in the browser.
  * Provide an option which unlocks the agent memory XMX limitation (by default it was 1G). Add agent.useXmxLimit=false in ${NGRINDER_AGENT_HOME}/agent.conf to enable this
  * Make the Tomcat stoppable by shutdown command.
  * Use record instead of wrap by default in the script.
  * Make a user be able to define new statistics.
  * Provide several more useful plugin interface.
  * Fix the bugs in which the some sampling points are missing in the final result.


- Bug Fix
  * [NGRINDER-511] - Make the agent server mode optional
  * [NGRINDER-512] - Provide a user defined statistic chart
  * [NGRINDER-516] - Fix typo errors
  * [NGRINDER-517] - Make the tomcat stoppable
  * [NGRINDER-519] - Delete pid when agent stop command run is succeeded
  * [NGRINDER-527] - Make the sampling count accurate and speed up the updateStatistics
  * [NGRINDER-528] - Make the agent monitor data is saved in DB
  * [NGRINDER-537] - Fix the bug which show then a test report panel in few seconds even when a user click test config panel


- Improvement
  * [NGRINDER-509] - Make the monitor data visible in realtime
  * [NGRINDER-513] - Make basic_template use record not wrap
  * [NGRINDER-514] - Make the agent connection error more visible
  * [NGRINDER-515] - Delete unnecessary packages.
  * [NGRINDER-518] - Make logs viewable in browser
  * [NGRINDER-520] - Collect network usage data from agent and make it stoppable when overflow
  * [NGRINDER-523] - Add an option to enable agent processes to use more than 1G memory
  * [NGRINDER-522] - Make network usage status available in agent monitor
  * [NGRINDER-524] - Make the resource / lib folder feature more explicit
  * [NGRINDER-526] - Make an agent connect to the console with controller ip
  * [NGRINDER-529] - Make plugin be able to see the currently attaching agent info
  * [NGRINDER-530] - Split physical network check to plugin
  * [NGRINDER-531] - Delete copyright
  * [NGRINDER-532] - Fix the missing region info in the bad network situation
  * [NGRINDER-533] - Make chart Y value 10 at minimum
  * [NGRINDER-534] - Modify Layout to show the main content bigger
  * [NGRINDER-539] - Make console host name used in agent.conf

3.1.1 (2013.01.31)
==================

> We're proudly releasing nGrinder 3.1.1 to speed up the test and enhance the usability.
With some bug fix, the following improvements are for the effective operations of nGrinder.

- New features and changes:
  * Speed up the test execution. Now all tests will take 5 seconds to execute a test.
  * Embed jython 2.5.3 and json.jar in the default libraries.
  * Make the agent takes less memory. now agent process need 50mb at minimum
  * Shows the response byte per second and mean time to first byte in the test running panel.
  * Change the default QnA site into nabble not github.
  * Change Process and Thread calculation logic more stable to support more than 300 users. Please delete the previous ${NGRINDER_HOME}/process_and_thread_policy.js in advance.

 
- Bug Fix
  * [NGRINDER-482] - Add grinder.processes and grinder.threads as properties during script validation
  * [NGRINDER-488] - Add a error message when there are no sampling was performed.
  * [NGRINDER-496] - Make user not to be able to change the underlying SecurityManager
  * [NGRINDER-497] - Make the current pc IP resolution more stable
  * [NGRINDER-498] - Make security mode work
  * [NGRINDER-504] - Show script initialize error in log
  * [NGRINDER-505] - Test Comment should not be cloned
  * [NGRINDER-506] - Block duplicated validation


- Improvement
  * [NGRINDER-478] - Distribute the un-predefined resources (e.g images)
  * [NGRINDER-479] - Speed up nGrinder performance reducing distribution cost (4~6sec) / add json.org library as default lib as well.
  * [NGRINDER-480] - Make each perftest folder distributed in several folders
  * [NGRINDER-481] - Move the question mark in logs to just right of log title
  * [NGRINDER-483] - Make nabble as a QnA Site
  * [NGRINDER-484] - Resolve locale cookie conflict issue
  * [NGRINDER-485] - Add cacheManager be accessible  from the script console
  * [NGRINDER-489] - Add control.followRedirects and to control.timeout in the basic template script
  * [NGRINDER-490] - Make all external links opened in the separate window
  * [NGRINDER-491] - Make the minimum Xms smaller to support more processes.
  * [NGRINDER-492] - Make sure the script is saved when user leave the script editor without save.
  * [NGRINDER-494] - Increase the default vuser count to 300
  * [NGRINDER-495] - Modify the process and thread policy to support 1000 vusers
  * [NGRINDER-499] - Include jython-2.5.3.jar as default lib
  * [NGRINDER-500] - Modify test termination condition as half of test failures for last 10 sec
  * [NGRINDER-502] - Add the mean time to first byte and response byte per second.
  * [NGRINDER-503] - Make ngrinder supports more than 2 billion test

3.1.0 (2013.01.07)
==================

> We're proudly releasing nGrinder 3.1 targeting the large organization.
With some bug fix, the following improvements are for the effective operations of nGrinder.

- New features and changes:
  * Support multiple region. User now can select one of multiple network regions to choose the agents depending on the test target location. All controllers in regions need to use a directory on network file system for controllers's home directory.
    - See http://www.cubrid.org/wiki_ngrinder/entry/controller-clustering-guide
  * Share user accounts. Now a user can share his permission to other users. Then the other user can start/check test with his rights.
    - http://www.cubrid.org/wiki_ngrinder/entry/user-permission
  * Modified monitoring function to not to use DB to save monitor data. Now the monitor data will be saved in a data file of the test directory.
  * Enable the super user role. Super user can check all user's tests and scripts without the permission for system configuration.
  * Enhance the default process and thread count calculation.  process count is configured in the much better way.
    - See http://www.cubrid.org/wiki_ngrinder/entry/advanced-controller-configuration
  * Add dynamic system configuration. You don't need to restart the server for the most of configuration changes.
    - See http://www.cubrid.org/wiki_ngrinder/entry/advanced-controller-configuration
  * Make agent much stable by providing the appropriate memory size (Reducing OOM)
	- See http://www.cubrid.org/wiki_ngrinder/entry/agent-configuration-guide
  * Add "Mean time to first byte" chart in the detailed report.
  * Change the license from BDS to Apache 2.0


- Minor improvement
  * Support CUBRID HA to remove the single of point failure. Make it stable as much as possible.
    - See http://www.cubrid.org/wiki_ngrinder/entry/advanced-controller-configuration
  * Support SHA256 password encoding depending on the configuration. Secure your account with the better password encoding.
    - See http://www.cubrid.org/wiki_ngrinder/entry/advanced-controller-configuration
  * Support the custom help site. Make your own site to help people.
    - See http://www.cubrid.org/wiki_ngrinder/entry/advanced-controller-configuration


3.0.4 (2012.12.04)
==================

> Hot fix for nGrinder 3.0
 Managing files using SVN Clients is now working.

- Bug
	[NGRINDER-385] - Managing files using SVN client is not working

3.0.3 (2012.11.XX)
==================

> Hot fix for nGrinder 3.0
  Mostly fix the bug which happens if there are more than 1000 tests.

- Bug Fix

  * [NGRINDER-367] - Tests over 1000 is not supported
  * [NGRINDER-368] - Agent approval in the agent page 2 is not supported
  * [NGRINDER-369] - Test stop is delayed when there are more than 1 test to be stop
  * [NGRINDER-371] - Resize script select box.

- Improvement

  * [NGRINDER-370] - Add plugin structure to make test stoppable base on the sampling.


3.0.2 (2012.11.01)
==================

> Hot fix for nGrinder 3.0
  Mostly we made this version work on Ubuntu

- Bug Fix
  * [NGRINDER-332] - User specific agents show max agents value 0
  * [NGRINDER-333] - agent ip is always 127.0.0.1 in Ubuntu
  * [NGRINDER-334] - Error - Abnormally Testing (TOO_LOW_TPS)
  * [NGRINDER-335] - Cannot stop Test when status is "Abnormally testing"
  * [NGRINDER-336] - When security is on, the logs are not passed from agents
  * [NGRINDER-337] - Real IP should be bound to SingleConsole
  * [NGRINDER-338] - Not able to login when restart server while logined
  * [NGRINDER-344] - Mean Test time graph is wrongly calculated
  * [NGRINDER-346] - Each test name is not visible in accumulated statistics
  * [NGRINDER-351] - Log monitor does not work after first log retrieval
  * [NGRINDER-357] - Create the user repo even when quick test is performed.
  * [NGRINDER-362] - Show runcount when user use runcount in report


- Improvement
  * [NGRINDER-352] - Add sampling listener scheme on update method in SingleConsole
  * [NGRINDER-354] - Make validation timeout longer (to 20 sec)
  * [NGRINDER-355] - Make the script console is only available where 50M perm gen memory is reserved.
  * [NGRINDER-356] - Make basic_template more stable
  * [NGRINDER-359] - Add the agent stop by admin
  * [NGRINDER-361] - Make stop agent more stable
  * [NGRINDER-363] - Hide user path on validation result
  * [NGRINDER-365] - Block to run test with 0 test

3.0.1 (2012.10.31)
==================

> Hot fix for nGrinder 3.0

- Bug Fix

  * [NGRINDER-331] - Jar classpath is not passed to the agents


3.0 (2012.10.29)
================

> First Official Version

- Bug Fix
  * [NGRINDER-232] - Running Time is wrong
  * [NGRINDER-238] - If the 1st minute of Test Report and the 1st minutes of Report_in_Detail are much different
  * [NGRINDER-287] - Table titles are overlapped on running page
  * [NGRINDER-310] - Check a finished test, Running_Page was displayed for a few seconds
  * [NGRINDER-317] - Input testing tag as "1,2,3,4" , save, was saved as "1" "2" "3" "4"
  * [NGRINDER-323] - Make max runcount / max runhour checked
  * [NGRINDER-325] - Running_time is wrong
  * [NGRINDER-152] - Make security mode work.


- Improvement
  * [NGRINDER-281] - Provide batch/shell for easy exeuction
  * [NGRINDER-320] - "Action" background color isn't grey when there isn't any script
  * [NGRINDER-314] - Provide servlet filter plugin
  * [NGRINDER-315] - When user provide the agent by himself, the user agent will will used only for the the given user.


3.0-b3 (2012.10.22)
===================

> Third beta version.

- Bug Fix
  * [NGRINDER-282] - Create a error showing page. For bad request which doesn't exist, redirect to index page.
  * [NGRINDER-283] - Provide DB upgrade feature
  * [NGRINDER-306] - Fixed a but to search with tag. If user selected tag to search items in perftest list page, then he can't search all items.
  * [NGRINDER-309] - Change script console as runtime component to speed up the test
  * [NGRINDER-312] - Fixed the bug about name format validation. Changed the minimal length of name to 3.


- Improvement
  * [NGRINDER-248] - Improved the editing in test configuration page.
  * [NGRINDER-288] - Improved to transfer Python module file to agents
  * [NGRINDER-301] - Provided agent shutdown script
  * [NGRINDER-302] - Delete all unused SystemDataModel DB fields.
  * [NGRINDER-303] - Modified to add option to create lib and resource folder when creating script.
  * [NGRINDER-304] - Set Max Agent Size as the min value b/w agentManager.getMaxAgentSizePerConsole(), agentManager.getAllApprovedAgents().size()
  * [NGRINDER-305] - Make admin be able to see user script.


3.0-b2 (2012.10.12)
===================

> Second beta version.

- change list
  * [NGRINDER-295]	Only English character and numbers are allowed in user id.
  * [NGRINDER-294]	Fixed a bug about statistic calculation error if there are several sub-tests in script.
  * [NGRINDER-290]	Not to start monitor if loading Sigar libraries error
  * [NGRINDER-282]	Create a error showing in index page
  * [NGRINDER-281]	Provided batch/shell for easy execution
  * [NGRINDER-299]	Fixed the problem of adding duplicated tag
  * [NGRINDER-298]	Added gray ball when user stop the perftest
  * No Issue		Fixed a bug in user creation.


3.0-b1 (2012.10.05)
===================
> First beta version.
