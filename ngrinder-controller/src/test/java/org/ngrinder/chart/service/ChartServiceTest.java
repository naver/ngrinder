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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;
import org.ngrinder.AbstractNGNinderTransactionalTest;
import org.ngrinder.chart.repository.JavaMonitorRepository;
import org.ngrinder.chart.repository.SystemMonitorRepository;
import org.ngrinder.monitor.controller.model.JavaDataModel;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since
 */
public class ChartServiceTest extends AbstractNGNinderTransactionalTest {

	@Autowired
	private JavaMonitorRepository javaRepository;

	@Autowired
	private SystemMonitorRepository systemRepository;

	@Test
	@Rollback(false)
	public void testSaveJavaMonitorInfo() {
		JavaDataModel javaInfo = newJavaData(20120719010101L);
		javaRepository.save(javaInfo);

		JavaDataModel infoInDb = javaRepository.findOne(javaInfo.getId());
		assertTrue(infoInDb.getId().equals(javaInfo.getId())
				&& infoInDb.getCpuUsedPercentage() == javaInfo.getCpuUsedPercentage());
	}

	/**
	 * Used to add mock monitor data
	 */
	// @Test
	public void addMockMonitorData() {
		int i = 1000 * 20;
		long colTime = 20120719010101L;
		while (i > 0) {
			JavaDataModel javaInfo = newJavaData(colTime);
			javaRepository.save(javaInfo);
			colTime++;
			i--;
		}

		i = 1000 * 20;
		colTime = 20120719010101L;
		while (i > 0) {
			SystemDataModel sysInfo = newSysData(colTime);
			systemRepository.save(sysInfo);
			colTime++;
			i--;
		}
	}

	private JavaDataModel newJavaData(long colTime) {
		JavaDataModel javaInfo = new JavaDataModel();
		javaInfo.setIp("10.0.0.1");
		javaInfo.setCollectTime(colTime);
		javaInfo.setCpuUsedPercentage(RandomUtils.nextFloat());
		javaInfo.setHeapMaxMemory(2048000);
		int used = RandomUtils.nextInt(2048000);
		javaInfo.setHeapUsedMemory(used);
		javaInfo.setNonHeapMaxMemory(1024000);
		used = RandomUtils.nextInt(1024000);
		javaInfo.setNonHeapUsedMemory(1024000 - used);
		javaInfo.setThreadCount(RandomUtils.nextInt(20));
		javaInfo.setPort(12345);
		return javaInfo;
	}

	private SystemDataModel newSysData(long colTime) {
		SystemDataModel sysInfo = new SystemDataModel();
		sysInfo.setIp("10.0.0.1");
		sysInfo.setCollectTime(colTime);
		sysInfo.setCpuUsedPercentage(RandomUtils.nextFloat());
		sysInfo.setIdleCpuValue(RandomUtils.nextFloat());
		sysInfo.setPort(12345);
		sysInfo.setTotalMemory(4096000);
		sysInfo.setFreeMemory(4096000 - RandomUtils.nextInt(2048000));
		sysInfo.setTotalCpuValue(4);
		return sysInfo;
	}

	@Test
	public void testSaveSystemMonitorInfo() {
		SystemDataModel sysInfo = newSysData(20120719010101L);
		systemRepository.save(sysInfo);

		SystemDataModel infoInDb = systemRepository.findOne(sysInfo.getId());
		assertTrue(infoInDb.getId().equals(sysInfo.getId())
				&& infoInDb.getCpuUsedPercentage() == sysInfo.getCpuUsedPercentage());
	}

	@Test
	public void testGetJavaMonitorData() {
		long startTime = 20120719010101L;
		long endTime = 20120719010201L;
		List<JavaDataModel> infoList = javaRepository.findAllByIpAndCollectTimeBetween("10.0.0.1", startTime, endTime);
		assertThat(infoList.size(), is(10));
	}

	@Test
	public void testGetSystemMonitorData() {
		long startTime = 20120719010101L;
		long endTime = 20120719010201L;
		List<SystemDataModel> infoList = systemRepository.findAllByIpAndCollectTimeBetween("10.0.0.1", startTime,
				endTime);
		assertThat(infoList.size(), is(101));
	}

}
