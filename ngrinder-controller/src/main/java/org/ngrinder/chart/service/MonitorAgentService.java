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
package org.ngrinder.chart.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.model.AgentInfo;
import org.ngrinder.chart.repository.MonitorDataRepository;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.monitor.controller.MonitorExecuteManager;
import org.ngrinder.monitor.controller.domain.MonitorAgentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Agent monitor data service.
 * 
 * @author Tobi
 * @since 3.0
 */
@Service
public class MonitorAgentService implements NGrinderConstants {

	private static final Logger LOG = LoggerFactory.getLogger(MonitorAgentService.class);

	@Autowired
	private MonitorDataRepository monitorDataRepository;

	@Autowired
	@Qualifier("dynamicCacheManager")
	private EhCacheCacheManager dynamicCacheManager;
	
	@PostConstruct
	public void setMonitorReferenceMap() {
		Cache cache = dynamicCacheManager.getCache(CACHE_NAME_DISTRIBUTED_MAP);
		if (cache.get(CACHE_NAME_MONITOR_REFERENCE_MAP) == null) {
			cache.put(CACHE_NAME_MONITOR_REFERENCE_MAP, new ConcurrentHashMap<String, MutableInt>());
		}
		@SuppressWarnings("unchecked")
		ConcurrentHashMap<String, MutableInt> monitorWorkerRefMap = (ConcurrentHashMap<String, MutableInt>)cache.get(CACHE_NAME_MONITOR_REFERENCE_MAP).get();
		MonitorExecuteManager.getInstance().setMonitorWorkerRefMap(monitorWorkerRefMap);
		LOG.debug("cache:{}", monitorWorkerRefMap);
	}

	//just for debug and test
	@Scheduled(fixedDelay = 5000)
	public void test() {
		Cache cache = dynamicCacheManager.getCache(CACHE_NAME_DISTRIBUTED_MAP);
		@SuppressWarnings("unchecked")
		ConcurrentHashMap<String, MutableInt> monitorWorkerRefMap = (ConcurrentHashMap<String, MutableInt>)cache.get(CACHE_NAME_MONITOR_REFERENCE_MAP).get();
		LOG.debug("cache:{}", monitorWorkerRefMap);
	}
	
	/**
	 * add a set of agents to the monitor manager, and start the monitor job.
	 * @param  agents {@link AgentInfo} agents
	 */
	public void addMonitorAgents(Set<AgentInfo> agents) {
		for (AgentInfo agent : agents) {
			MonitorAgentInfo monitorAgentInfo = 
					MonitorAgentInfo.getSystemMonitor(agent.getIp(), agent.getPort(), monitorDataRepository);
			MonitorExecuteManager.getInstance().addAgentMonitor(agent.getIp(), monitorAgentInfo);
		}
		refreshCache();
	}
	
	/**
	 * Remove agents, and stop monitoring.
	 * 
	 * @param agents {@link AgentInfo} set
	 */
	public void removeMonitorAgents(Set<AgentInfo> agents) {
		for (AgentInfo agent : agents) {
			try {
				MonitorExecuteManager.getInstance().removeAgentMonitor(agent.getIp());
				LOG.debug("Remove nGrinder Monitor for:{} successfully.", agent.getIp());
			} catch (Exception e) {
				LOG.error("Error occurs while remove monitor for {}", agent.getIp());
			}
		}
		refreshCache();
	}

	/**
	 * Only for UnitTest.
	 */
	void removeAllAgent() {
		MonitorExecuteManager.getInstance().removeAllAgent();
		refreshCache();
	}
	
	private void refreshCache() {
		Cache cache = dynamicCacheManager.getCache(CACHE_NAME_DISTRIBUTED_MAP);
		cache.put(CACHE_NAME_MONITOR_REFERENCE_MAP, MonitorExecuteManager.getInstance().getMonitorWorkerRefMap());
	}

}
