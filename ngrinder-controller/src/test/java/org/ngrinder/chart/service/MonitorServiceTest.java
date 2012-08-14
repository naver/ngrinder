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

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.junit.Test;
import org.ngrinder.chart.AbstractChartTransactionalTest;
import org.ngrinder.monitor.controller.model.JavaDataModel;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class MonitorServiceTest extends AbstractChartTransactionalTest {
	
	@Autowired
	private MonitorService monitorService;

	/**
	 * Test method for {@link org.ngrinder.chart.service.MonitorService#saveJavaMonitorInfo(org.ngrinder.monitor.controller.model.JavaDataModel)}.
	 */
	@Test
	public void testSaveAndGetJavaMonitorInfo() {
		long startTime = NumberUtils.toLong(df.format(new Date()));
		JavaDataModel javaInfo = newJavaData(startTime, "10.0.0.1");
		monitorService.saveJavaMonitorInfo(javaInfo);
		List<JavaDataModel> infoList = monitorService.getJavaMonitorData("10.0.0.1", startTime, startTime);
		assertThat(infoList.size(), is(1));
	}

	/**
	 * Test method for {@link org.ngrinder.chart.service.MonitorService#saveSystemMonitorInfo(org.ngrinder.monitor.controller.model.SystemDataModel)}.
	 */
	@Test
	public void testSaveAndGetSystemMonitorInfo() {
		long startTime = NumberUtils.toLong(df.format(new Date()));
		SystemDataModel sysInfo = newSysData(startTime, "10.0.0.1");
		monitorService.saveSystemMonitorInfo(sysInfo);
		List<SystemDataModel> infoList = monitorService.getSystemMonitorData("10.0.0.1", startTime, startTime);
		assertThat(infoList.size(), is(1));
	}

}
