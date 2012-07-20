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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.ngrinder.monitor.controller.domain.MonitorAgentInfo;

public class MonitorExecuteManager {
	private ScheduledExecutorService scheduler;
	private long firstTime;
	private long interval;
	private MonitorAgentInfo[] agentInfo;
	private boolean running;
	private String key;

	private List<MonitorExecuteWorker> monitorExecuteWorkers = new ArrayList<MonitorExecuteWorker>();

	public MonitorExecuteManager(final String key, final int interval, final MonitorAgentInfo[] agentInfo) {
		this(key, interval, interval, agentInfo);
	}

	public MonitorExecuteManager(final String key, final int interval, final long firstTime,
			final MonitorAgentInfo[] agentInfo) {
		this.key = key;
		this.interval = interval;
		this.firstTime = firstTime;
		this.agentInfo = agentInfo;
		scheduler = Executors.newScheduledThreadPool(this.agentInfo.length);
	}

	public void start() {
		if (!isRunning()) {
			for (int i = 0; i < this.agentInfo.length; i++) {
				MonitorExecuteWorker mew = new MonitorExecuteWorker(key, agentInfo[i]);
				monitorExecuteWorkers.add(mew);
				scheduler.scheduleAtFixedRate(mew, firstTime, interval, TimeUnit.SECONDS);
			}
			running = true;
		}
	}

	public void stop() {
		if (isRunning()) {
			scheduler.shutdown();
			for (MonitorExecuteWorker mew : monitorExecuteWorkers) {
				mew.close();
			}
			running = false;
		}
	}

	public boolean isRunning() {
		return running;
	}
}
