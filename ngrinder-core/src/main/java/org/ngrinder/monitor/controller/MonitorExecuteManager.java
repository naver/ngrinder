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
package org.ngrinder.monitor.controller;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.monitor.controller.domain.MonitorAgentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Used to manage the monitoring job.
 *
 * @author Mavlarn
 * @since 3.0
 */
public final class MonitorExecuteManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(MonitorExecuteManager.class);
	
	private long firstTime = 1;
	private long interval = 1;

	private Map<String, ScheduledExecutorService> schedulerMap =
			new ConcurrentHashMap<String, ScheduledExecutorService>();
	private Map<String, MonitorExecuteWorker> monitorWorkerMap =
			new ConcurrentHashMap<String, MonitorExecuteWorker>();

	//used to save the counter of how many reference to use one monitor worker.
	private Map<String, MutableInt> monitorWorkerRefMap = new ConcurrentHashMap<String, MutableInt>();

	private static MonitorExecuteManager instance = new MonitorExecuteManager();
	
	//instance class, avoid creating object
	private MonitorExecuteManager() {
	}
	
	public static MonitorExecuteManager getInstance() {
		return instance;
	}
	
	public void setMonitorWorkerRefMap(ConcurrentHashMap<String, MutableInt> monitorWorkerRefMap) {
		this.monitorWorkerRefMap = monitorWorkerRefMap;
	}
	
	public Map<String, MutableInt> getMonitorWorkerRefMap() {
		return this.monitorWorkerRefMap;
	}
	
	/**
	 * add a new monitoring job.
	 * If there is already a job monitoring on that server, just increase the counter.
	 * @param key is the key of the monitoring worker
	 * @param agent is the monitoring target
	 */
	public void addAgentMonitor(String key, MonitorAgentInfo agent) {
		MutableInt refCount = monitorWorkerRefMap.get(agent.getIp());
		if (refCount == null || refCount.intValue() == 0) {
			MonitorExecuteWorker worker = new MonitorExecuteWorker(key, agent);
			ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
			scheduler.scheduleAtFixedRate(worker, firstTime, interval, TimeUnit.SECONDS);
			monitorWorkerRefMap.put(agent.getIp(), new MutableInt(1));
			monitorWorkerMap.put(agent.getIp(), worker);
			schedulerMap.put(agent.getIp(), scheduler);
			LOG.debug("Add monitoring worker for {} successfully.", agent.getIp());
		} else {
			refCount.increment();
			LOG.debug("Monitoring worker for {} already exist.", agent.getIp());
		}
	}
	
	/**
	 * stop and remove all monitoring job.
	 */
	public void removeAllAgent() {
		for (Entry<String, MonitorExecuteWorker> each: monitorWorkerMap.entrySet()) {
			schedulerMap.get(each.getKey()).shutdown();
			each.getValue().close();
		}
		monitorWorkerRefMap.clear();
		monitorWorkerMap.clear();
		schedulerMap.clear();
	}
	
	/**
	 * remove a monitoring job if there is only one test monitoring on this server.
	 * @param agentIP is the IP address of monitoring target server
	 */
	public void removeAgentMonitor(String agentIP) {
		MutableInt refCount = checkNotNull(monitorWorkerRefMap.get(agentIP));
		refCount.decrement();
		//stop and remove worker if only there is no any monitor reference on this agent
		if (refCount.intValue() <= 0) {
			schedulerMap.get(agentIP).shutdown();
			schedulerMap.remove(agentIP);
			monitorWorkerMap.get(agentIP).close();
			monitorWorkerMap.remove(agentIP);
			monitorWorkerRefMap.remove(agentIP);
		}
	}

}
