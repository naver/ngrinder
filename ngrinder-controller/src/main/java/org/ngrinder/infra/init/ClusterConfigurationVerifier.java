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

import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkArgument;
import static org.ngrinder.common.util.Preconditions.checkState;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;

import net.sf.ehcache.Ehcache;

import org.apache.commons.io.FileUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.region.service.RegionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Component;

/**
 * Verify clustering is set up well. such as check the region is not duplicated. check if they use
 * same home.
 * 
 * @since3.2
 */
@Component
public class ClusterConfigurationVerifier {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClusterConfigurationVerifier.class);

	@Autowired
	private Config config;

	@Autowired
	private EhCacheCacheManager cacheManager;

	@Autowired
	private RegionService regionService;

	private Cache cache;

	private File systemConfFile;

	/**
	 * Check cluster configurations.
	 * 
	 * @throws IOException
	 *             exception
	 */
	@PostConstruct
	public void init() throws IOException {
		if (config.isClustered() && !config.isTestMode()) {
			systemConfFile = config.getHome().getSubFile("system.conf");
			cache = cacheManager.getCache("controller_home");
			config.addSystemConfListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					updateSystemConfFingerPrintToCache(systemConfFile);
				}
			});
			checkHome();
			checkDB();
		}
	}

	/**
	 * check if they use same home.
	 * 
	 * @throws IOException
	 *             exception
	 */
	private void checkHome() throws IOException {
		checkArgument(systemConfFile.exists(), "File does not exist: %s", systemConfFile);
		String systemConfFingerPrint = String.valueOf(FileUtils.checksumCRC32(systemConfFile));
		for (Object eachKey : ((Ehcache) (cache.getNativeCache())).getKeys()) {
			try {
				ValueWrapper valueWrapper = cache.get(eachKey);
				if (valueWrapper != null && valueWrapper.get() != null) {
					checkState(systemConfFingerPrint.equals(valueWrapper.get()),
									"Thie controller's ${NGRINDER_HOME} conflicts with other controller(" + eachKey
													+ "), Please check if each controller"
													+ " shares same ngrinder home folder.");
				}
			} catch (Exception e) {
				noOp();
			}

		}
		updateSystemConfFingerPrintToCache(systemConfFile);
	}

	/**
	 * check if they use CUBRID in cluster mode.
	 */
	private void checkDB() {
		String db = config.getDatabaseProperties().getProperty("database", "NONE").toLowerCase();
		checkState("cubrid".equals(db), "%s is unable to be used in cluster mode and Please use CUBRID !", db);
	}

	private void updateSystemConfFingerPrintToCache(File systemConfFile) {
		try {
			String systemConfFingerPrint = String.valueOf(FileUtils.checksumCRC32(systemConfFile));
			cache.put(regionService.getCurrent(), systemConfFingerPrint);
		} catch (IOException e) {
			LOGGER.error("Error while updating system.conf fingerprint into cache.", e);
		}
	}
}
