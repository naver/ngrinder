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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.ConsoleManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Script Runner for maintenance.
 *
 * This class has the jython instance and put the most important class instances as variables in the jython. Admin and
 * super user can run any jython code to print out or modify the internal ngrinder states.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@Profile("production")
@Controller
@RequestMapping("/operation/script_console")
@PreAuthorize("hasAnyRole('A')")
public class ScriptConsoleController extends BaseController implements ApplicationContextAware {

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


	/**
	 * Run the given script. The run result is stored in "result" of the given model.
	 *
	 * @param script script
	 * @param model  model
	 * @return operation/script_console
	 */
	@RequestMapping("")
	public String run(@RequestParam(value = "script", defaultValue = "") String script, Model model) {
		if (StringUtils.isNotBlank(script)) {
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("Groovy");
			engine.put("applicationContext", this.applicationContext);
			engine.put("agentManager", this.agentManager);
			engine.put("agentManagerService", this.agentManagerService);
			engine.put("regionService", this.regionService);
			engine.put("consoleManager", this.consoleManager);
			engine.put("userService", this.userService);
			engine.put("perfTestService", this.perfTestService);
			engine.put("tagService", this.tagService);
			engine.put("fileEntryService", this.fileEntryService);
			engine.put("config", getConfig());
			engine.put("pluginManager", this.pluginManager);
			engine.put("cacheManager", this.cacheManager);
			engine.put("user", getCurrentUser());
			final StringWriter out = new StringWriter();
			PrintWriter writer = new PrintWriter(out);
			engine.getContext().setWriter(writer);
			engine.getContext().setErrorWriter(writer);
			try {
				Object result = engine.eval(script);
				result = out.toString() + "\n" + ObjectUtils.defaultIfNull(result, "");
				model.addAttribute("result", result);
			} catch (ScriptException e) {
				model.addAttribute("result", out.toString() + "\n" + e.getMessage());
			}
		}
		model.addAttribute("script", script);
		return "operation/script_console";
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
