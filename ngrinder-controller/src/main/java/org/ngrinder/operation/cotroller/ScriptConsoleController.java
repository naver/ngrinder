/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.operation.cotroller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.infra.annotation.RuntimeOnlyController;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.ConsoleManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.user.service.UserService;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Script Runner for maintenance.
 * 
 * This class has the jython instance and put the most important class instances as variables in the jython. Admin and
 * super user can run any jython code to print out or modify the internal ngrinder states.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@RuntimeOnlyController
@RequestMapping("/operation/script_console")
@PreAuthorize("hasAnyRole('A')")
public class ScriptConsoleController extends NGrinderBaseController implements ApplicationContextAware {
	private static final int SCRIPT_CONSOLE_PYTHON_EXPIRE_TIMEOUT = 30000;

	private ApplicationContext applicationContext;

	@Autowired
	private AgentManager agentManager;

	@Autowired
	private AgentManagerService agentManagerService;

	@Autowired
	private ConsoleManager consoleManager;

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private UserService userService;

	@Autowired
	private RegionService regionService;

	@Autowired
	private PluginManager pluginManager;

	@Autowired
	private TagService tagService;

	@Autowired
	private CacheManager cacheManager;

	private PythonInterpreter interp;

	/**
	 * Initialize Jython and put several managers and services into the jython context.
	 */
	@PostConstruct
	public void init() {
		Iterator<MemoryPoolMXBean> iter = ManagementFactory.getMemoryPoolMXBeans().iterator();
		MemoryUsage usage = null;
		while (iter.hasNext()) {
			MemoryPoolMXBean item = (MemoryPoolMXBean) iter.next();
			if (item.getName().contains("Perm Gen")) {
				usage = item.getUsage();

			}
		}
		File jythonCache = new File(FileUtils.getTempDirectory(), "jython");
		jythonCache.mkdirs();
		System.setProperty("python.cachedir", jythonCache.getAbsolutePath());
		if (usage != null && isPermGenMemoryEnough(usage)) {
			interp = new PythonInterpreter();
			intVars(interp);
		}
	}

	/**
	 * Destroy bean.
	 */
	@PreDestroy
	public void destroy() {
		if (interp != null) {
			clearVar(interp);
			interp.cleanup();
		}
	}

	private boolean isPermGenMemoryEnough(MemoryUsage usage) {
		return (usage.getMax() - usage.getUsed()) > 50000000;
	}

	protected void clearVar(PythonInterpreter interp) {
		interp.set("applicationContext", null);
		interp.set("agentManager", null);
		interp.set("agentManagerService", null);
		interp.set("regionService", null);
		interp.set("consoleManager", null);
		interp.set("userService", null);
		interp.set("perfTestService", null);
		interp.set("tagService", null);
		interp.set("fileEntryService", null);
		interp.set("config", null);
		interp.set("pluginManager", null);
		interp.set("cacheManager", null);
	}

	protected void intVars(PythonInterpreter interp) {
		interp.set("applicationContext", this.applicationContext);
		interp.set("agentManager", this.agentManager);
		interp.set("agentManagerService", this.agentManagerService);
		interp.set("regionService", this.regionService);
		interp.set("consoleManager", this.consoleManager);
		interp.set("userService", this.userService);
		interp.set("perfTestService", this.perfTestService);
		interp.set("tagService", this.tagService);
		interp.set("fileEntryService", this.fileEntryService);
		interp.set("config", getConfig());
		interp.set("pluginManager", this.pluginManager);
		interp.set("cacheManager", this.cacheManager);
	}

	/**
	 * Run the given script. The run result is stored in "result" of the given model.
	 * 
	 * @param script
	 *            script
	 * @param model
	 *            model
	 * @return operation/script_console
	 */
	@RequestMapping("")
	public String runScript(@RequestParam(value = "script", required = false) String script, Model model) {
		if (interp == null) {
			model.addAttribute("script", script);
			model.addAttribute("result", "Script Console is disabled due to memory config."
					+ " Please set up Perm Gen memory more than 200M");
		} else if (StringUtils.isNotBlank(script)) {
			String result = processPython(script);
			model.addAttribute("script", script);
			model.addAttribute("result", result);
		}
		return "operation/script_console";
	}

	/**
	 * Run the jython script.
	 * 
	 * @param script
	 *            script
	 * @return result printed in stdout and err
	 */
	public String processPython(final String script) {
		try {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			synchronized (interp) {
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						interp.cleanup();
						interp.setOut(bos);
						interp.setErr(bos);
						interp.exec(script);
						interp.cleanup();
					}
				});
				thread.start();
				thread.join(SCRIPT_CONSOLE_PYTHON_EXPIRE_TIMEOUT);
				if (thread.isAlive()) {
					thread.interrupt();
				}
			}
			return bos.toString();
		} catch (Exception e) {
			String message = ExceptionUtils.getMessage(e);
			message = message + "\n" + ExceptionUtils.getStackTrace(e);
			return message;
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
