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

import net.sf.ehcache.Ehcache;
import org.apache.commons.io.FileUtils;
import org.ngrinder.common.constant.DatabaseConstants;
import org.ngrinder.infra.config.Config;
import org.ngrinder.region.model.RegionInfo;
import org.ngrinder.region.service.RegionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import static org.ngrinder.common.util.NoOp.noOp;
import static org.ngrinder.common.util.Preconditions.checkArgument;
import static org.ngrinder.common.util.Preconditions.checkState;

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


	/**
	 * Check cluster configurations.
	 *
	 * @throws IOException exception
	 */
	@PostConstruct
	public void init() throws IOException {
		if (config.isClustered() && !config.isDevMode()) {
			checkDB();
		}
	}

	/**
	 * check if they use CUBRID in cluster mode.
	 */
	private void checkDB() {
		String db = config.getDatabaseProperties().getProperty(DatabaseConstants.PROP_DATABASE_TYPE).toLowerCase();
		if (!db.equals("cubrid")) {
			final String dbURL = config.getDatabaseProperties().getProperty(DatabaseConstants.PROP_DATABASE_URL, "");
			checkState(dbURL.startsWith("tcp://"), "Wrong database.url configuration " + dbURL + "\n" +
					"When cluster mode is enabled, embedded H2 db can not be used. Use cubrid or Use H2 TCP server");
		}
	}

}
