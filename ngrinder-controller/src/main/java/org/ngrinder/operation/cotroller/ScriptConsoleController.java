package org.ngrinder.operation.cotroller;

import java.io.ByteArrayOutputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.infra.config.Config;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.ConsoleManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.user.service.UserService;
import org.python.util.PythonInterpreter;
import org.springframework.beans.BeansException;
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

	private PythonInterpreter interp;

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
	}

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
	 * @return stdout and err
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
				thread.run();
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
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
