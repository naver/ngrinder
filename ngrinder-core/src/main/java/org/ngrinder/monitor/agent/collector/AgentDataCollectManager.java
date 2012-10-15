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
package org.ngrinder.monitor.agent.collector;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.agent.AgentMXBeanStorage;
import org.ngrinder.monitor.agent.mxbean.core.MXBean;

/**
 * 
 * Agent data collect manager class, to start/stop the data collector jobs.
 *
 * @author Mavlarn
 * @since 2.0
 */
public final class AgentDataCollectManager {
	private static final Logger LOG = LoggerFactory.getLogger(AgentDataCollectManager.class);

	private boolean isRunning = false;
	private ScheduledExecutorService scheduler;
	private static final AgentDataCollectManager INSTANCE = new AgentDataCollectManager();

	private AgentDataCollectManager() {
	}

	public static AgentDataCollectManager getInstance() {
		return INSTANCE;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public int getInterval() {
		return MonitorConstants.DEFAULT_MONITOR_COLLECTOR_INTERVAL;
	}

	/**
	 * start a scheduler for the data collector jobs.
	 */
	public void start() {
		int collectorCount = AgentMXBeanStorage.getInstance().getSize();
		scheduler = Executors.newScheduledThreadPool(collectorCount);
		if (!isRunning()) {
			Collection<MXBean> mxBeans = AgentMXBeanStorage.getInstance().getMXBeans();
			for (MXBean mxBean : mxBeans) {
				scheduler.scheduleAtFixedRate(mxBean.gainAgentDataCollector(), 0L, getInterval(), TimeUnit.SECONDS);
				LOG.info("Agent collector: {} started.", mxBean.gainAgentDataCollector().getClass().getSimpleName());
			}
			LOG.info("Agent collector start (interval :{} s).", getInterval());

			isRunning = true;
		}
	}

	/**
	 * stop the scheduler for data collecting jobs.
	 */
	public void stop() {
		if (isRunning()) {
			scheduler.shutdown();
			isRunning = false;
			LOG.info("Agent collector End");
		}
	}
}
