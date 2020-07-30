package org.ngrinder.operation.cotroller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.agent.service.AgentService;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.hazelcast.HazelcastService;
import org.ngrinder.infra.plugin.PluginManager;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.ConsoleManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.user.service.UserContext;
import org.ngrinder.user.service.UserService;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;

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
@RequiredArgsConstructor
public class ScriptConsoleApiController {

	private final ApplicationContext applicationContext;

	private final AgentManager agentManager;

	private final AgentService agentService;

	private final ConsoleManager consoleManager;

	private final PerfTestService perfTestService;

	private final FileEntryService fileEntryService;

	private final UserService userService;

	private final RegionService regionService;

	private final PluginManager pluginManager;

	private final TagService tagService;

	private final CacheManager cacheManager;

	private final UserContext userContext;

	private final Config config;

	private final HazelcastService hazelcastService;

	/**
	 * Run the given script. The run result is stored in "result" of the given model.
	 *
	 * @param param script json
	 * @return script run result
	 */
	@PostMapping({"", "/"})
	public Map<String, Object> run(@RequestBody Map<String, Object> param) {
		String script = (String) param.getOrDefault("script", "");
		String result = "";

		if (StringUtils.isNotBlank(script)) {
			ScriptEngine engine = new ScriptEngineManager().getEngineByName("Groovy");
			engine.put("applicationContext", this.applicationContext);
			engine.put("agentManager", this.agentManager);
			engine.put("agentService", this.agentService);
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
			engine.put("hazelcastService", this.hazelcastService);
			final StringWriter out = new StringWriter();
			PrintWriter writer = new PrintWriter(out);
			engine.getContext().setWriter(writer);
			engine.getContext().setErrorWriter(writer);

			try {
				Object evalResult = engine.eval(script);
				result = Objects.toString(evalResult, "");
			} catch (ScriptException e) {
				result = e.getMessage();
			} finally {
				String outString = out.toString();
				if (StringUtils.isNotEmpty(outString)) {
					result = outString + "\n" + result;
				}
			}
		}
		return buildMap("result", result);
	}

}
