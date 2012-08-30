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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.SystemUtils;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.agent.collector.process.JavaMonitorProcessor;
import org.ngrinder.monitor.agent.mxbean.JavaMonitoringData;
import org.ngrinder.monitor.share.JVMUtils;
import org.ngrinder.monitor.share.domain.JavaInfo;
import org.ngrinder.monitor.share.domain.JavaInfoForEach;
import org.ngrinder.monitor.share.domain.JavaVirtualMachineInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentJavaDataCollector extends AgentDataCollector {
	private static final Logger LOG = LoggerFactory.getLogger(AgentJavaDataCollector.class);

	private Map<Integer, JavaVirtualMachineInfo> cache;
	private Map<JavaVirtualMachineInfo, JavaMonitorProcessor> processors;
	private ExecutorService pool;
	private CompletionService<JavaInfoForEach> cs;
	private long count = 0;

	@Override
	public synchronized void refresh() {
		cache = JVMUtils.getAllJVMs();

		processors = new HashMap<JavaVirtualMachineInfo, JavaMonitorProcessor>();
		Iterator<Integer> keys = cache.keySet().iterator();
		while (keys.hasNext()) {
			Integer key = keys.next();
			JavaVirtualMachineInfo jvmInfo = cache.get(key);
			JavaMonitorProcessor processor = new JavaMonitorProcessor(jvmInfo);
			processors.put(jvmInfo, processor);

		}

		pool = Executors.newFixedThreadPool(cache.size() == 0 ? 1 : cache.size());
		cs = new ExecutorCompletionService<JavaInfoForEach>(pool);
	}

	@Override
	public void run() {
		JavaMonitoringData javaMonitoringData = (JavaMonitoringData) getMXBean(MonitorConstants.JAVA);
		JavaInfo javaInfo = execute();
		javaMonitoringData.addNotification(javaInfo);
		javaMonitoringData.setJavaInfo(javaInfo);
	}

	public synchronized JavaInfo execute() {
		JavaInfo javaInfo = new JavaInfo();

		for (Entry<JavaVirtualMachineInfo, JavaMonitorProcessor> jj : processors.entrySet()) {
			JavaMonitorProcessor processor = jj.getValue();
			cs.submit(processor);
		}

		for (int i = 0; i < processors.size(); i++) {
			try {
				javaInfo.addJavaInfoForEach((JavaInfoForEach) cs.take().get());
			} catch (Exception e) {
				if ((count++) % 60 == 0) {
					if (SystemUtils.IS_OS_WINDOWS) {
						LOG.error("Error while getting java perf data");
						LOG.error("You should run agent in administrator permission");
					} else {
						LOG.error("Error while getting java perf data", e);
					}
				}
			}
		}

		javaInfo.setCollectTime(System.currentTimeMillis());
		return javaInfo;
	}
}
