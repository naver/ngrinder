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

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.monitor.controller.domain.MonitorAgentInfo;
import org.ngrinder.monitor.controller.domain.MonitorRecoder;
import org.ngrinder.monitor.controller.model.JavaDataModel;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.monitor.share.domain.JavaInfo;
import org.ngrinder.monitor.share.domain.JavaInfoForEach;
import org.ngrinder.monitor.share.domain.SystemInfo;
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
		checkNotNull(javaInfo);
		List<JavaInfoForEach> javaInfoForEachs = javaInfo.getJavaInfoForEach();
		long collTime = DateUtil.getCollectTimeInLong(new Date(javaInfo.getCollectTime()));
		for (JavaInfoForEach javaInfoForEach : javaInfoForEachs) {
			JavaDataModel javaDataModel = new JavaDataModel();
			javaDataModel.setCollectTime(collTime);
			javaDataModel.setKey(agentInfo.getIp());
			javaDataModel.setIp(agentInfo.getIp());
			javaDataModel.setPort(agentInfo.getPort());
			String newDispName = StringUtils.abbreviate(javaInfoForEach.getDisplayName(), 200);
			javaDataModel.setDisplayName(newDispName);
			javaDataModel.setHeapMaxMemory(javaInfoForEach.getHeapMemory().getMax());
			javaDataModel.setHeapUsedMemory(javaInfoForEach.getHeapMemory().getUsed());
			javaDataModel.setNonHeapMaxMemory(javaInfoForEach.getNonHeapMemory().getMax());
			javaDataModel.setNonHeapUsedMemory(javaInfoForEach.getNonHeapMemory().getUsed());
			javaDataModel.setCpuUsedPercentage(javaInfoForEach.getJavaCpuUsedPercentage());
			javaDataModel.setPid(javaInfoForEach.getPid());
			javaDataModel.setThreadCount(javaInfoForEach.getThreadCount());
			javaDataModel.setUptime(javaInfoForEach.getUptime());

			javaMonitorRepository.save(javaDataModel);
		}

	}

	@Override
	public void recoderSystemInfo(String key, SystemInfo systemInfo, MonitorAgentInfo agentInfo) {
		checkNotNull(systemInfo);
		SystemDataModel systemDataModel = new SystemDataModel(systemInfo);
		systemDataModel.setKey(agentInfo.getIp());
		systemDataModel.setIp(agentInfo.getIp());
		systemDataModel.setPort(agentInfo.getPort());
		systemMonitorRepository.save(systemDataModel);

	}

	@Override
	public void after() {
		// do nothing
	}

}
