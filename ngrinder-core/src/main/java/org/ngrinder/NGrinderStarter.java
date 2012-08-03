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
package org.ngrinder;

import java.util.Set;

import net.grinder.AgentControllerDaemon;
import net.grinder.common.GrinderException;

import org.ngrinder.common.util.ThreadUtil;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.agent.AgentServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class to start agent or monitor.
 * 
 * @author Mavlarn
 * @since 3.0
 */
public class NGrinderStarter {

	private final Logger LOG = LoggerFactory.getLogger(NGrinderStarter.class);

	private boolean localAttachmentSupported;
	
	public NGrinderStarter() {
		try {
			Class.forName("com.sun.tools.attach.VirtualMachine");
			Class.forName("sun.management.ConnectorAddressLink");
			localAttachmentSupported = true;
		} catch (NoClassDefFoundError x) {
			LOG.error(x.getMessage(), x);
			localAttachmentSupported = false;
		} catch (ClassNotFoundException x) {
			LOG.error(x.getMessage(), x);
			localAttachmentSupported = false;
		}
	}

	private void startMonitor() {
		int port = MonitorConstants.DEFAULT_AGENT_PORT;
		Set<String> dataCollectors = MonitorConstants.DEFAULT_DATA_COLLECTOR;
		Set<Integer> jvmPids = MonitorConstants.DEFAULT_JVM_PID;

		LOG.info("**************************");
		LOG.info("* Start nGrinder Monitor *");
		LOG.info("**************************");
		LOG.info("* Local JVM link support :{}", localAttachmentSupported);
		try {
			AgentServer.getInstance().init(port, dataCollectors, jvmPids);
			AgentServer.getInstance().start();
		} catch (Exception e) {
			LOG.error("ERROR:", e);
		}

		ThreadUtil.sleep(4000);
		AgentServer.getInstance().refreshJavaDataCollect();
		LOG.info("* Refresh java data monitoring.e *");
		ThreadUtil.sleep(10000);
	}
	
	private void startAgent() {
		LOG.info("*************************");
		LOG.info("* Start nGrinder Agent **");
		LOG.info("*************************");
		LOG.info("Not implemented yet.");
		
		AgentControllerDaemon agentController = new AgentControllerDaemon();
		try {
			agentController.run();
		} catch (GrinderException e) {
			LOG.error("ERROR:", e);
		}
	}
	
	public static void main(String[] args) {
		NGrinderStarter starter = new NGrinderStarter();
		if (args != null && args.length > 0 && args[0].equals("-m")) {
			//just start monitor
			starter.startMonitor();
		} else {
			starter.startAgent();
		}
	}

}
