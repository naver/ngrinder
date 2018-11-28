package org.ngrinder.packages;

import org.springframework.stereotype.Component;

import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;

import static org.ngrinder.common.util.CollectionUtils.buildMap;

@Component("monitorPackageHandler")
public class MonitorPackageHandler extends PackageHandler {

	@Override
	public Map<String, Object> getConfigParam(String regionName, String controllerIP, int port, String owner) {
		return buildMap("monitorPort", (Object) String.valueOf(port));
	}

	@Override
	public Set<String> getPackageDependentLibs(URLClassLoader urlClassLoader) {
		return super.getDependentLibs(urlClassLoader);
	}

	@Override
	public String getModuleName() {
		return "ngrinder-monitor";
	}

	@Override
	public String getBasePath() {
		return "ngrinder-monitor/";
	}

	@Override
	public String getLibPath() {
		return "ngrinder-monitor/lib/";
	}

	@Override
	public String getShellScriptsPath() {
		return "classpath*:ngrinder-sh/monitor/*";
	}

	@Override
	public String getTemplateName() {
		return "agent_monitor.conf";
	}

	@Override
	public String getDependenciesFileName() {
		return "monitor-dependencies.txt";
	}
}
