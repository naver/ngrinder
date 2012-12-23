/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.monitor.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.model.Home;
import org.ngrinder.infra.config.Config;
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
public class MonitorControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private MonitorController monitorController;

	@Autowired
	private Config config;

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
