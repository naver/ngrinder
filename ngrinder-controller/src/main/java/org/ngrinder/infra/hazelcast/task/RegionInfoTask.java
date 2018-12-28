package org.ngrinder.infra.hazelcast.task;

import com.google.common.collect.Sets;
import com.hazelcast.spring.context.SpringAware;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.util.NetworkUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.region.model.RegionInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.HashSet;
import java.util.concurrent.Callable;

/**
 * Task for getting region info from clustered controller.
 *
 * @since 3.5.0
 */
@SpringAware
public class RegionInfoTask implements Callable<RegionInfo>, Serializable {

	@Autowired
	private transient AgentManager agentManager;

	@Autowired
	private transient Config config;

	public RegionInfo call() {
		HashSet<AgentIdentity> newHashSet = Sets.newHashSet(agentManager.getAllAttachedAgents());
		final String regionIP = StringUtils.defaultIfBlank(config.getCurrentIP(), NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS);
		return new RegionInfo(config.getRegion(), regionIP, config.getControllerPort(), newHashSet);
	}
}
