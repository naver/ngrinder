package org.ngrinder.infra.hazelcast.task;

import com.hazelcast.spring.context.SpringAware;
import org.ngrinder.infra.config.Config;
import org.ngrinder.region.model.RegionInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static net.grinder.util.NetworkUtils.DEFAULT_LOCAL_HOST_ADDRESS;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.ngrinder.common.constant.CacheConstants.REGION_ATTR_KEY;
import static org.ngrinder.common.constant.CacheConstants.SUBREGION_ATTR_KEY;
import static org.ngrinder.common.util.RegionUtils.convertSubregionsStringToSet;

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
		Map<String, String> regionWithSubregion = config.getRegionWithSubregion();
		Set<String> subregion = convertSubregionsStringToSet(regionWithSubregion.get(SUBREGION_ATTR_KEY));
		return new RegionInfo(regionWithSubregion.get(REGION_ATTR_KEY), subregion, regionIP, config.getControllerPort());
	}
}
