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
package org.ngrinder.chart.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.ngrinder.chart.AbstractChartTransactionalTest;
import org.ngrinder.chart.service.MonitorService;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

/**
 * MonitorController Test class.
 *
 * @author Mavlarn
 * @since
 */
public class MonitorControllerTest extends AbstractChartTransactionalTest {

	private static final String DATE_FORMAT = "yyyyMMddHHmmss";
	private static final DateFormat df = new SimpleDateFormat(DATE_FORMAT);
	
	@Autowired
	private MonitorController monitorController;

	@Autowired
	private MonitorService monitorService;
	
	@Test
	public void testGetCurrentMonitorData() {
		ModelMap model = new ModelMap();
		String rtnStr = monitorController.getCurrentMonitorData(model, "127.0.0.1");
		LOG.debug("Current monitor data for ip:{} is\n{}", "127.0.0.1", rtnStr);
	}

	@Test
	public void testMonitorDataNoTimeParam() {
		ModelMap model = new ModelMap();
		String rtnStr = monitorController.getMonitorData(model, "127.0.0.1", null, null, 0);
		LOG.debug("Current monitor data for ip:{} is\n{}", "127.0.0.1", rtnStr);
	}

	@Test
	public void testMonitorData() throws ParseException {
		ModelMap model = new ModelMap();
		Date endTime = new Date();
		long endTimelong = Long.valueOf(df.format(new Date()));
		long startTimeLong = endTimelong - 10 *60 * 1000; //10 minutes before

		//add record, make sure getMonitorData() can get data
		SystemDataModel systemInfo = newSysData(endTimelong, "10.0.0.1");
		monitorService.saveSystemMonitorInfo(systemInfo);
		
		Date startTime = df.parse(String.valueOf(startTimeLong));
		String rtnStr = monitorController.getMonitorData(model, "127.0.0.1", startTime, endTime, 500);
		LOG.debug("Current monitor data for ip:{} is\n{}", "127.0.0.1", rtnStr);
	}

}
