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
package org.ngrinder.monitor;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.util.ThreadUtil;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.agent.AgentMonitorServer;
import org.springframework.core.io.ClassPathResource;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public abstract class AbstractChartTransactionalTest extends AbstractNGrinderTransactionalTest {

	protected static final String DATE_FORMAT = "yyyyMMddHHmmss";
	protected static final DateFormat df = new SimpleDateFormat(DATE_FORMAT);

	private static void setupSigar() {
		try {
			ClassPathResource classPathResource = new ClassPathResource("native_lib/.sigar_shellrc");
			String nativeLib = classPathResource.getFile().getParentFile().getAbsolutePath();
			String javaLib = System.getProperty("java.library.path");
			if (!javaLib.contains("native_lib")) {
				System.setProperty("java.library.path", nativeLib + File.pathSeparator + javaLib);
			}
			System.out.println("Java Lib Path : " + System.getProperty("java.library.path"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@BeforeClass
	public static void startMonitorServer() {
		setupSigar();
		AgentConfig agentConfig = new AgentConfig();
		agentConfig.init();

		MonitorConstants.init(agentConfig);
		LOG.info("**************************");
		LOG.info("* Start nGrinder Monitor *");
		LOG.info("**************************");
		LOG.info("* Colllect SYSTEM data. **");
		try {
			//start with moth java and system collector
			Set<String> collector = MonitorConstants.SYSTEM_DATA_COLLECTOR;
			AgentMonitorServer.getInstance().init(MonitorConstants.DEFAULT_MONITOR_PORT, collector);
			AgentMonitorServer.getInstance().start();
		} catch (Exception e) {
			LOG.error("ERROR: {}", e.getMessage());
			LOG.debug("Error while starting Monitor", e);
		}
		ThreadUtil.sleep(2000);
	}
	
	@AfterClass
	public static void stopMonitorServer() {
		AgentMonitorServer.getInstance().stop();
		ThreadUtil.sleep(1000);
	}	
	
}
