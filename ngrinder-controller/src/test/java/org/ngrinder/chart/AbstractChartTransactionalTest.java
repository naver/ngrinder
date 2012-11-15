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
package org.ngrinder.chart;

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
