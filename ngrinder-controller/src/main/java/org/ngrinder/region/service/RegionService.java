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
package org.ngrinder.region.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.grinder.util.thread.InterruptibleRunnable;

import org.apache.commons.io.FileUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.schedule.ScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Region service class. This class responsible to keep the status of available regions.
 * 
 * @author Mavlarn
 * @author JunHo Yoon
 * @since 3.1
 */
@Service
public class RegionService {

	private static final Logger LOG = LoggerFactory.getLogger(RegionService.class);

	@Autowired
	private Config config;

	@Autowired
	private ScheduledTask scheduledTask;

	/**
	 * Set current region into cache, using the IP as key and region name as value.
	 * 
	 */
	@PostConstruct
	public void initRegion() {
		if (config.isCluster()) {
			scheduledTask.addScheduledTaskEvery3Sec(new InterruptibleRunnable() {
				@Override
				public void interruptibleRun() {
					checkRegionUdate();
				}

			});
		}
	}

	void checkRegionUdate() {
		String region = config.getRegion();
		File file = new File(config.getHome().getControllerShareDirectory(), region);
		if (!file.exists()) {
			try {
				FileUtils.writeStringToFile(file, config.getCurrentIP(), "UTF-8");
			} catch (IOException e) {
			}
		} else {
			try {
				FileUtils.touch(file);
			} catch (IOException e) {
			}
		}
		LOG.trace("Add Region: {}:{} into cache.", region);
	}

	/**
	 * Destroy method. this method is responsible to delete our current region from dist cache.
	 */
	@PreDestroy
	public void destroy() {
		if (config.isCluster()) {
			File file = new File(config.getHome().getControllerShareDirectory(), config.getRegion());
			FileUtils.deleteQuietly(file);
		}
	}

	/**
	 * Get current region. This method returns where this service is running.
	 * 
	 * @return current region.
	 */
	public String getCurrentRegion() {
		return config.getRegion();
	}

	/**
	 * Get region list of all clustered controller.
	 * 
	 * @return region list
	 */
	public List<String> getRegions() {
		List<String> regions = new ArrayList<String>();
		if (config.isCluster()) {
			for (File each : config.getHome().getControllerShareDirectory().listFiles()) {
				if (System.currentTimeMillis() - (1000 * 60) < each.lastModified()) {
					regions.add(each.getName());
				}
			}
		}
		return regions;
	}

	Config getConfig() {
		return config;
	}

	void setConfig(Config config) {
		this.config = config;
	}
}
