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
package org.ngrinder.chart.repository;

import java.util.List;

import org.apache.commons.lang.time.DateFormatUtils;
import org.ngrinder.monitor.controller.domain.MonitorAgentInfo;
import org.ngrinder.monitor.controller.domain.MonitorRecoder;
import org.ngrinder.monitor.controller.model.JavaDataModel;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.monitor.share.domain.JavaInfo;
import org.ngrinder.monitor.share.domain.JavaInfoForEach;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Monitor data repository.
 * 
 * @author Tobi
 * @since
 * @date 2012-7-23
 */
@Component
public class MonitorDataRepository implements MonitorRecoder {

	private static final Logger LOG = LoggerFactory.getLogger(MonitorDataRepository.class);

	@Autowired
	private JavaMonitorRepository javaMonitorRepository;

	@Autowired
	private SystemMonitorRepository systemMonitorRepository;

	@Override
	public void before() {
		// do nothing
	}

	@Override
	public void recoderJavaInfo(String key, JavaInfo javaInfo, MonitorAgentInfo agentInfo) {
		if (javaInfo != null) {
			List<JavaInfoForEach> javaInfoForEachs = javaInfo.getJavaInfoForEach();
			JavaDataModel javaDataModel = new JavaDataModel();
			for (JavaInfoForEach javaInfoForEach : javaInfoForEachs) {
				javaDataModel.setKey(agentInfo.getIp());
				javaDataModel.setIp(agentInfo.getIp());
				javaDataModel.setPort(agentInfo.getPort());
				javaDataModel.setDisplayName(javaInfoForEach.getDisplayName());
				String collectTimeStr = DateFormatUtils.format(javaInfo.getCollectTime(), "yyyyMMddHHmmss");
				javaDataModel.setCollectTime(Long.valueOf(collectTimeStr));
				javaDataModel.setHeapMaxMemory(javaInfoForEach.getHeapMemory().getMax());
				javaDataModel.setHeapUsedMemory(javaInfoForEach.getHeapMemory().getUsed());
				javaDataModel.setNonHeapMaxMemory(javaInfoForEach.getNonHeapMemory().getMax());
				javaDataModel.setNonHeapUsedMemory(javaInfoForEach.getNonHeapMemory().getUsed());
				javaDataModel.setCpuUsedPercentage(javaInfoForEach.getJavaCpuUsedPercentage());
				javaDataModel.setPid(javaInfoForEach.getPid());
				javaDataModel.setThreadCount(javaInfoForEach.getThreadCount());
				javaDataModel.setUptime(javaInfoForEach.getUptime());

				javaMonitorRepository.save(javaDataModel);
				LOG.debug("javaDataInsert: {}", javaDataModel.getIp());
			}
		}

	}

	@Override
	public void recoderSystemInfo(String key, SystemInfo systemInfo, MonitorAgentInfo agentInfo) {
		if (systemInfo != null) {
			SystemDataModel systemDataModel = new SystemDataModel();
			systemDataModel.setKey(agentInfo.getIp());
			systemDataModel.setIp(agentInfo.getIp());
			systemDataModel.setPort(agentInfo.getPort());
			systemDataModel.setSystem(systemInfo.getSystem().toString());
			String collectTimeStr = DateFormatUtils.format(systemInfo.getCollectTime(), "yyyyMMddHHmmss");
			systemDataModel.setCollectTime(Long.valueOf(collectTimeStr));
			systemDataModel.setTotalCpuValue(systemInfo.getTotalCpuValue());
			systemDataModel.setIdleCpuValue(systemInfo.getIdlecpu());
			systemDataModel.setLoadAvg1(systemInfo.getLoadAvgs()[0]);
			systemDataModel.setLoadAvg5(systemInfo.getLoadAvgs()[1]);
			systemDataModel.setLoadAvg15(systemInfo.getLoadAvgs()[2]);
			systemDataModel.setFreeMemory(systemInfo.getFreeMemory());
			systemDataModel.setTotalMemory(systemInfo.getTotalMemory());
			systemDataModel.setCpuUsedPercentage(systemInfo.getCPUUsedPercentage());

			systemMonitorRepository.save(systemDataModel);
			LOG.debug("systemDataInsert: {}", systemDataModel.getIp());
		}

	}

	@Override
	public void after() {
		// do nothing
	}

}
