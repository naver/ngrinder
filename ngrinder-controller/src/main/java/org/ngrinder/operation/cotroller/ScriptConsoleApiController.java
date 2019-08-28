package org.ngrinder.operation.cotroller;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.ConsoleManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.user.service.UserContext;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static org.ngrinder.common.util.CollectionUtils.buildMap;

/**
 * Script Runner for maintenance.
 * <p>
 * This class has the groovy instance and put the most important class instances as variables in the groovy.
 * Admin can run any groovy code to print out or modify the internal ngrinder states.
 *
 * @since 3.0
 */
@Profile("production")
@RestController
@RequestMapping("/operation/script_console/api")
@PreAuthorize("hasAnyRole('A')")
public class ScriptConsoleApiController {

	@Autowired
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

	@Autowired
	private UserContext userContext;

	@Autowired
	private Config config;

	/**
	 * Run the given script. The run result is stored in "result" of the given model.
	 *
	 * @param param script json
	 * @return script run result
	 */
	@PostMapping({"", "/"})
	public Map<String, Object> run(@RequestBody Map<String, Object> param) {
		String script = (String) param.getOrDefault("script", "");
		String result = null;

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
			engine.put("config", this.config);
			engine.put("pluginManager", this.pluginManager);
			engine.put("cacheManager", this.cacheManager);
			engine.put("user", this.userContext.getCurrentUser());
			final StringWriter out = new StringWriter();
			PrintWriter writer = new PrintWriter(out);
			engine.getContext().setWriter(writer);
			engine.getContext().setErrorWriter(writer);
			try {
				Object evalResult = engine.eval(script);
				result = out.toString() + "\n" + ObjectUtils.defaultIfNull(evalResult, "");
			} catch (ScriptException e) {
				result = out.toString() + "\n" + e.getMessage();
			}
		}
		return buildMap("result", result);
	}

}
