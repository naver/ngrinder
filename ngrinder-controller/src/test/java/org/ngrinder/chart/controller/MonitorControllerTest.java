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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Set;

import net.grinder.AgentController;
import net.grinder.common.processidentity.AgentIdentity;

import org.junit.Test;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.chart.AbstractChartTransactionalTest;
import org.ngrinder.common.model.Home;
import org.ngrinder.infra.config.Config;
import org.ngrinder.perftest.service.AgentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ModelMap;

/**
 * MonitorController Test class.
 * 
 * @author Mavlarn
 * @since
 */
public class MonitorControllerTest extends AbstractChartTransactionalTest {

	@Autowired
	private MonitorController monitorController;

	@Autowired
	private Config config;

	@Autowired
	private AgentManager agentManager;

	@Autowired
	private AgentManagerService agentManagerService;

	@Test
	public void testGetCurrentMonitorData() {
		ModelMap model = new ModelMap();
		Set<AgentIdentity> allAttachedAgents = agentManager.getAllAttachedAgents();
		String rtnStr = monitorController.getCurrentMonitorData(model, 1L);
		LOG.debug("Current monitor data for ip:{} is\n{}", "127.0.0.1", rtnStr);
	}

	@Test
	public void testMonitorData() throws ParseException, IOException {
		ModelMap model = new ModelMap();
		String monitorIP = "127.0.0.1";
		long mockTestId = 1234567890;
		String mockPath = String.valueOf(mockTestId) + File.separator + "report";
		File mockTestReportFile = new ClassPathResource(mockPath).getFile();

		// set a mock home object to let it find the sample monitor file.
		Home realHome = config.getHome();
		Home mockHome = mock(Home.class);
		when(mockHome.getPerfTestReportDirectory(String.valueOf(mockTestId))).thenReturn(mockTestReportFile);
		ReflectionTestUtils.setField(config, "home", mockHome);

		String rtnStr = monitorController.getMonitorData(model, mockTestId, monitorIP, 700);
		LOG.debug("Monitor data for ip:{} is\n{}", "127.0.0.1", rtnStr);

		// reset the home object
		ReflectionTestUtils.setField(config, "home", realHome);
	}

}
