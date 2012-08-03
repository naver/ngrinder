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
package org.ngrinder.monitor.agent.collector.process;

import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.Callable;

import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.share.CachedMBeanClient;
import org.ngrinder.monitor.share.domain.JavaInfoForEach;
import org.ngrinder.monitor.share.domain.JavaMemory;
import org.ngrinder.monitor.share.domain.JavaVirtualMachineInfo;
import org.ngrinder.monitor.share.domain.MBeanClient;

import com.sun.management.OperatingSystemMXBean;

public class JavaMonitorProcessor implements Callable<JavaInfoForEach> {

	private JavaVirtualMachineInfo jvmInfo;

	private long prevUpTime = 0;
	private long prevProcessCpuTime = 0;

	public JavaMonitorProcessor(JavaVirtualMachineInfo jvmInfo) {
		this.jvmInfo = jvmInfo;
	}

	public JavaInfoForEach call() throws Exception {
		JavaInfoForEach javaInfo = null;

		MBeanClient mbeanClient = CachedMBeanClient.getMBeanClient(jvmInfo);

		if (!mbeanClient.isConnected()) {
			mbeanClient.connect();
		}

		// TODO: If mbeanClient was not connected?
		if (mbeanClient.isConnected()) {
			// mbeanClient.flush();

			javaInfo = new JavaInfoForEach();
			// java Process name
			javaInfo.setDisplayName(jvmInfo.getDisplayName());
			if (javaInfo.getDisplayName() != null && javaInfo.getDisplayName().startsWith("nGrinder")) {
				javaInfo.setDisplayName(MonitorConstants.DEFALUT_MONITOR_DISPLAY_NAME);
			}
			// java process id
			javaInfo.setPid(jvmInfo.getVmid());
			javaInfo.setUptime(mbeanClient.getRuntimeMXBean().getUptime());
			javaInfo.setThreadCount(mbeanClient.getThreadMXBean().getThreadCount());

			JavaMemory heapMemory = new JavaMemory();
			MemoryMXBean memoryBean = mbeanClient.getMemoryMXBean();
			heapMemory.setCommitted(memoryBean.getHeapMemoryUsage().getCommitted());
			heapMemory.setInit(memoryBean.getHeapMemoryUsage().getInit());
			heapMemory.setMax(memoryBean.getHeapMemoryUsage().getMax());
			heapMemory.setUsed(memoryBean.getHeapMemoryUsage().getUsed());
			javaInfo.setHeapMemory(heapMemory);

			JavaMemory nonHeapMemory = new JavaMemory();
			nonHeapMemory.setCommitted(memoryBean.getNonHeapMemoryUsage().getCommitted());
			nonHeapMemory.setInit(memoryBean.getNonHeapMemoryUsage().getInit());
			nonHeapMemory.setMax(memoryBean.getNonHeapMemoryUsage().getMax());
			nonHeapMemory.setUsed(memoryBean.getNonHeapMemoryUsage().getUsed());
			javaInfo.setNonHeapMemory(nonHeapMemory);

			OperatingSystemMXBean osbean = mbeanClient.getSunOperatingSystemMXBean();
			RuntimeMXBean runbean = mbeanClient.getRuntimeMXBean();
			int nCPUs = osbean.getAvailableProcessors();
			long upTime = runbean.getUptime();
			long processCpuTime = osbean.getProcessCpuTime();

			if (prevUpTime > 0L && upTime > prevUpTime) {
				long elapsedCpu = processCpuTime - prevProcessCpuTime;
				long elapsedTime = upTime - prevUpTime;
				//TODO : (elapsedTime * 10000F * nCPUs) > 0 is always right?
				javaInfo.setJavaCpuUsedPercentage(Math.min(100F, elapsedCpu / (elapsedTime * 10000F * nCPUs)));
			} else {
				javaInfo.setJavaCpuUsedPercentage(0.001f);
			}
			prevUpTime = upTime;
			prevProcessCpuTime = processCpuTime;
		}

		return javaInfo;
	}
}
