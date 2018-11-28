package org.ngrinder.packages;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;

import static org.ngrinder.common.util.CollectionUtils.newHashMap;

@Component("agentPackageHandler")
public class AgentPackageHandler extends PackageHandler {

	private Logger LOGGER = LoggerFactory.getLogger(AgentPackageHandler.class);

	@Autowired
	private ScheduledTaskService scheduledTaskService;

	@PostConstruct
	public void cleanUpCachedPackageDir() {
		cleanUpPackageDir(true);

		scheduledTaskService.addFixedDelayedScheduledTask(new Runnable() {
			@Override
			public void run() {
				cleanUpPackageDir(false);
			}
		}, TIME_MILLIS_OF_DAY);
	}

	@Override
	public String getModuleName() {
		return "ngrinder-agent";
	}

	@Override
	public String getBasePath() {
		return "ngrinder-agent/";
	}

	@Override
	public String getLibPath() {
		return "ngrinder-agent/lib/";
	}

	@Override
	public String getShellScriptsPath() {
		return "classpath*:ngrinder-sh/agent/*";
	}

	@Override
	public String getTemplateName() {
		return "agent_agent.conf";
	}

	@Override
	public String getDependenciesFileName() {
		return "dependencies.txt";
	}

	@Override
	public Map<String, Object> getConfigParam(String regionName, String controllerIP, int port, String owner) {
		Map<String, Object> confMap = newHashMap();
		confMap.put("controllerIP", controllerIP);
		confMap.put("controllerPort", String.valueOf(port));
		if (StringUtils.isEmpty(regionName)) {
			regionName = "NONE";
		}
		if (StringUtils.isNotBlank(owner)) {
			if (StringUtils.isEmpty(regionName)) {
				regionName = "owned_" + owner;
			} else {
				regionName = regionName + "_owned_" + owner;
			}
		}
		confMap.put("controllerRegion", regionName);
		return confMap;
	}

	@Override
	public Set<String> getPackageDependentLibs(URLClassLoader urlClassLoader) {
		Set<String> libs = getDependentLibs(urlClassLoader);
		libs.add("ngrinder-core");
		libs.add("ngrinder-runtime");
		libs.add("ngrinder-groovy");
		return libs;
	}
}
