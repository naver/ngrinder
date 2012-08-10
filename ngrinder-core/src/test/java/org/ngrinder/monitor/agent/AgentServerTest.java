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
package org.ngrinder.monitor.agent;

import java.io.IOException;
import java.util.Set;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.Test;
import org.ngrinder.common.util.ThreadUtil;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.monitor.MonitorConstants;

public class AgentServerTest {
	private static final Logger LOG = LoggerFactory.getLogger(AgentServerTest.class);

	@Test
	public void startMonitor() throws MalformedObjectNameException, InstanceAlreadyExistsException,
			MBeanRegistrationException, NotCompliantMBeanException, NullPointerException, IOException {
		int port = 4096;
		Set<String> dataCollectors = MonitorConstants.DEFAULT_DATA_COLLECTOR;
		Set<Integer> jvmPids = MonitorConstants.DEFAULT_JVM_PID;

		LOG.info("******************************");
		LOG.info("* Start nGrinder Monitor Agent *");
		LOG.info("******************************");
		LOG.info("* Local JVM link support :{}", localAttachmentSupported);
		
		AgentConfig config = new AgentConfig();
		MonitorConstants.init(config);
		
		AgentMonitorServer.getInstance().init(port, dataCollectors, jvmPids);
		AgentMonitorServer.getInstance().start();

		ThreadUtil.sleep(4000);
		AgentMonitorServer.getInstance().refreshJavaDataCollect();
		LOG.info("* Refresh java data monitoring.e *");
		ThreadUtil.sleep(10000);
	}

	private static final boolean localAttachmentSupported;
	static {
		boolean supported;
		try {
			Class.forName("com.sun.tools.attach.VirtualMachine");
			Class.forName("sun.management.ConnectorAddressLink");
			supported = true;
		} catch (NoClassDefFoundError x) {
			LOG.error(x.getMessage(), x);
			supported = false;
		} catch (ClassNotFoundException x) {
			LOG.error(x.getMessage(), x);
			supported = false;
		}
		localAttachmentSupported = supported;
	}

}
