/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
					String region = config.getRegion();
					File file = new File(config.getHome().getControllerShareDirectory(), region);
					try {
						FileUtils.writeStringToFile(file, config.getCurrentIP(), "UTF-8");
					} catch (IOException e) {
					}
					LOG.trace("Add Region: {}:{} into cache.", region);
				}
			});
		}
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
}
