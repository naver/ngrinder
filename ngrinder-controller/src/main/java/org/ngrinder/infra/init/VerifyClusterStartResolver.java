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
package org.ngrinder.infra.init;

import static org.ngrinder.common.util.Preconditions.checkState;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;

import org.apache.commons.io.FileUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.region.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Component;

/**
 * Verify clustering is set up well. such as check the region is not duplicated.
 * check if they use same home.
 * 
 * @since3.2
 */
@Component
public class VerifyClusterStartResolver {

	@Autowired
	private Config config;

	@Autowired
	private EhCacheCacheManager cacheManager;

	@Autowired
	private RegionService regionService;
	
	private Cache cache;

	/**
	 * Check cluster starting
	 * 
	 * @throws IOException
	 */
	@PostConstruct
	public void verifyCluster() throws IOException {
		if (config.isCluster()) {
			checkExHome();
			checkUsedDB();
		}
	}

	/**
	 * check if they use same home.
	 * 
	 * @throws IOException
	 */
	private void checkExHome() throws IOException {
		File home = config.getHome().getDirectory();
		String homeFIleStamp = String.valueOf(FileUtils.checksumCRC32(new File(home, "system.conf")));
		cache = cacheManager.getCache("controller_home");
		for (Object eachKey : ((Ehcache) (cache.getNativeCache())).getKeys()) {
			ValueWrapper valueWrapper = cache.get(eachKey);
			if (valueWrapper != null && valueWrapper.get() != null) {
				checkState(homeFIleStamp.equals(valueWrapper.get()),
						"Controller's {NGRINDER_HOME} is conflict with other controller,Please check this !");
			}
		}
		cache.put(regionService.getCurrentRegion(), homeFIleStamp);
	}
	
	/**
	 * check if they use CUBRID in cluster mode
	 * 
	 */
	private void checkUsedDB() {
		String db = config.getDatabaseProperties().getProperty("database","NONE").toLowerCase();
		checkState("cubrid".equals(db),
				db+" is unable to be used in cluster mode and please using CUBRID !");
	}

}
