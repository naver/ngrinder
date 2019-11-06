package org.ngrinder.infra.hazelcast.task;

import com.hazelcast.spring.context.SpringAware;
import org.ngrinder.infra.config.Config;
import org.ngrinder.region.model.RegionInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.concurrent.Callable;

import static net.grinder.util.NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;

/**
 * Task for getting region info from clustered controller.
 *
 * @since 3.5.0
 */
@SpringAware
public class RegionInfoTask implements Callable<RegionInfo>, Serializable {

	@Autowired
	private transient Config config;

	public RegionInfo call() {
		final String regionIP = defaultIfBlank(config.getCurrentIP(), DEFAULT_LOCAL_HOST_ADDRESS);
		return new RegionInfo(config.getRegion(), regionIP, config.getControllerPort());
	}
}
