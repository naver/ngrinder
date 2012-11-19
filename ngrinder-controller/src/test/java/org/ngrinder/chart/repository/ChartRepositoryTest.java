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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;
import org.ngrinder.chart.AbstractChartTransactionalTest;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since 3.0
 */
public class ChartRepositoryTest extends AbstractChartTransactionalTest {

	@Autowired
	private SystemMonitorRepository systemRepository;

	/**
	 * Used to add mock monitor data
	 */
	// @Test
	public void addMockMonitorData() {
		int i = 1000 * 20;
		long colTime = 20120719010101L;

		while (i > 0) {
			SystemDataModel sysInfo = newSysData(colTime);
			systemRepository.save(sysInfo);
			colTime++;
			i--;
		}
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
	public void testGetSystemMonitorData() {
		long startTime = NumberUtils.toLong(df.format(new Date()));
		long endTime = startTime + 100;
		SystemDataModel sysInfo = newSysData(startTime);
		systemRepository.save(sysInfo);
		SystemDataModel infoInDb = systemRepository.findOne(sysInfo.getId());
		assertTrue(infoInDb.getId().equals(sysInfo.getId())
				&& infoInDb.getCpuUsedPercentage() == sysInfo.getCpuUsedPercentage());
		
		List<SystemDataModel> infoList = systemRepository.findAllByIpAndCollectTimeBetween("10.0.0.1", startTime,
				endTime);
		assertThat(infoList.size(), is(1));
		
		sysInfo = newSysData(startTime + 1);
		systemRepository.save(sysInfo);
		infoList = systemRepository.findAllByIpAndCollectTimeBetween("10.0.0.1", startTime, endTime);
		assertThat(infoList.size(), is(2));
	}
	
	private SystemDataModel newSysData(long colTime) {
		SystemDataModel sysInfo = new SystemDataModel();
		sysInfo.setIp("10.0.0.1");
		sysInfo.setCollectTime(colTime);
		sysInfo.setCpuUsedPercentage(RandomUtils.nextFloat());
		sysInfo.setPort(12345);
		sysInfo.setTotalMemory(4096000);
		sysInfo.setFreeMemory(4096000 - RandomUtils.nextInt(2048000));
		return sysInfo;
	}

}
