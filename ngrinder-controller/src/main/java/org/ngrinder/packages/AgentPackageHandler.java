package org.ngrinder.packages;

import lombok.RequiredArgsConstructor;
import org.ngrinder.agent.model.PackageDownloadInfo;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

import static org.ngrinder.common.util.CollectionUtils.newHashMap;

@Component("agentPackageHandler")
@RequiredArgsConstructor
public class AgentPackageHandler extends PackageHandler {

	private final ScheduledTaskService scheduledTaskService;

	@PostConstruct
	public void cleanUpCachedPackageDir() {
		cleanUpPackageDir(true);
		scheduledTaskService.addFixedDelayedScheduledTask(() -> cleanUpPackageDir(false), TIME_MILLIS_OF_DAY);
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
	public Map<String, Object> getConfigParam(PackageDownloadInfo packageDownloadInfo) {
		Map<String, Object> confMap = newHashMap();
		confMap.put("controllerIP", packageDownloadInfo.getConnectionIp());
		confMap.put("controllerPort", String.valueOf(packageDownloadInfo.getConnectionPort()));
		confMap.put("subregion", packageDownloadInfo.getSubregion());
		confMap.put("owner", packageDownloadInfo.getOwner());
		return confMap;
	}

	@Override
	public Set<String> getPackageDependentLibs() {
		Set<String> libs = getDependentLibs();
		libs.add("ngrinder-core");
		libs.add("ngrinder-runtime");
		libs.add("ngrinder-groovy");
		return libs;
	}
}
