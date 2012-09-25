/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.operation.cotroller;

import java.io.ByteArrayOutputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.ConsoleManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.user.service.UserService;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Script Runner for maintenance.
 * 
 * This class has the jython instance and put the most important class instances
 * as variable in the jython. Admin and super user can run any jython code to
 * print out or modify the internal ngrinder status.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Controller
@RequestMapping("/operation/scriptConsole")
@PreAuthorize("hasAnyRole('A', 'S')")
public class ScriptConsoleController extends NGrinderBaseController implements ApplicationContextAware {
	@Autowired
	private Config config;

	private ApplicationContext applicationContext;

	@Autowired
	private AgentManager agentManager;

	@Autowired
	private ConsoleManager consoleManager;

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private UserService userService;

	@Autowired
	private PluginManager pluginManager;
	
	private PythonInterpreter interp;

	/**
	 * Initialize Jython and puts several managers and services into jython
	 * context.
	 */
	@PostConstruct
	public void init() {
		interp = new PythonInterpreter();
		interp.set("applicationContext", this.applicationContext);
		interp.set("agentManager", this.agentManager);
		interp.set("consoleManager", this.consoleManager);
		interp.set("userService", this.userService);
		interp.set("perfTestService", this.perfTestService);
		interp.set("fileEntryService", this.fileEntryService);
		interp.set("config", this.config);
		interp.set("pluginManager", this.pluginManager);
	}

	/**
	 * Run script. The run result is stored in "result" of the given model.
	 * 
	 * @param script
	 *            script
	 * @param model
	 *            model
	 * @return "operation/scriptConsole"
	 */
	@RequestMapping("")
	public String runScript(@RequestParam(value = "script", required = false) String script, Model model) {
		if (StringUtils.isNotBlank(script)) {
			String result = processPython(script);
			model.addAttribute("script", script);
			model.addAttribute("result", result);
		}
		return "operation/scriptConsole";
	}

	/**
	 * Run python script.
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
				thread.join(30000);
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
