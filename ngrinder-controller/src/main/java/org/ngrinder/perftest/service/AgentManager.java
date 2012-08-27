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
package org.ngrinder.perftest.service;

import static org.ngrinder.common.util.CollectionUtils.selectSome;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import net.grinder.AgentControllerServerDaemon;
import net.grinder.SingleConsole;
import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.communication.AgentControllerCommunicationDefauts;
import net.grinder.console.communication.LogArrivedListener;
import net.grinder.engine.controller.AgentControllerIdentityImplementation;
import net.grinder.message.console.AgentControllerState;
import net.grinder.messages.console.AgentAddress;
import net.grinder.util.thread.ExecutorFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.agent.model.AgentInfo;
import org.ngrinder.agent.service.AgentService;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.infra.config.Config;
import org.ngrinder.monitor.controller.model.JavaDataModel;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Agent manager class.
 * 
 * This class runs {@link AgentControllerServerDaemon} internally and manage to agent connection.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
public class AgentManager implements NGrinderConstants {
	public static final Logger LOGGER = LoggerFactory.getLogger(AgentManager.class);
	private final AgentControllerServerDaemon agentControllerServer = new AgentControllerServerDaemon(
					AgentControllerCommunicationDefauts.DEFAULT_AGENT_CONTROLLER_SERVER_PORT);
	private static final int NUMBER_OF_THREAD = 3;
	private static final int AGENT_RUN_TIMEOUT_SECOND = 10;

	@Autowired
	private Config config;

	@Autowired
	private AgentService agentService;

	/**
	 * Initialize agent manager.
	 */
	@PostConstruct
	public void init() {
		agentControllerServer.start();
		agentControllerServer.addLogArrivedListener(new LogArrivedListener() {
			@Override
			public void logArrived(String testId, AgentAddress agentAddress, byte[] logs) {
				AgentControllerIdentityImplementation agentIdentity = (AgentControllerIdentityImplementation) agentAddress
								.getIdentity();

				if (logs == null || logs.length == 0) {
					LOGGER.error("Log is arrived from {} but no log content", agentIdentity.getIp());
				}
				File logDirectory = new File(config.getHome().getPerfTestDirectory(
								testId.replace("test_", "")), "logs");
				logDirectory.mkdirs();
				File logFile = null;
				try {
					logFile = new File(logDirectory, agentIdentity.getIp() + "-" + agentIdentity.getName()
									+ "-" + agentIdentity.getRegion() + "-log.zip");
					FileUtils.writeByteArrayToFile(logFile, logs);
				} catch (IOException e) {
					LOGGER.error("Error while write logs from {} to {}",
									agentAddress.getIdentity().getName(), logFile.getAbsolutePath());
					LOGGER.error("Error is following", e);
				}
			}
		});
	}

	public AgentControllerState getAgentControllerState(AgentIdentity agentIdentity) {
		return agentControllerServer.getAgentState(agentIdentity);
	}

	public Set<AgentIdentity> getAllAttachedAgents() {
		return agentControllerServer.getAllAvailableAgents();
	}

	public int getMaxAgentSizePerConsole() {
		return config.getSystemProperties().getPropertyInt("agent.maxsize",
						NGrinderConstants.MAX_AGENT_SIZE_PER_CONSOLE);
	}

	public int getMaxVuserPerAgent() {
		return config.getSystemProperties().getPropertyInt("agent.maxvuser",
						NGrinderConstants.MAX_VUSER_PER_AGENT);
	}

	public int getMaxRunCount() {
		return config.getSystemProperties().getPropertyInt("agent.maxruncount",
						NGrinderConstants.MAX_RUN_COUNT);
	}

	public int getMaxRunHour() {
		return config.getSystemProperties().getPropertyInt("agent.maxhour", NGrinderConstants.MAX_RUN_HOUR);
	}

	public AgentControllerIdentityImplementation getAgentIdentityByIp(String agentIP) {
		for (AgentIdentity agentIdentity : getAllAttachedAgents()) {
			AgentControllerIdentityImplementation eachAgentIdentity = (AgentControllerIdentityImplementation) agentIdentity;
			if (StringUtils.equals(eachAgentIdentity.getIp(), agentIP)) {
				return eachAgentIdentity;
			}
		}
		return null;
	}

	public Set<AgentIdentity> getAllFreeAgents() {
		return agentControllerServer.getAllFreeAgents();
	}

	public Set<AgentIdentity> getAllFreeApprovedAgents() {
		Set<AgentIdentity> allFreeAgents = agentControllerServer.getAllFreeAgents();
		return filterApprovedAgents(allFreeAgents);
	}

	/**
	 * Filter approved agents from given agents.
	 * 
	 * @param agents
	 *            all agents
	 * @return approved agents.
	 */
	public Set<AgentIdentity> filterApprovedAgents(Set<AgentIdentity> agents) {
		if (agents.size() == 0) {
			return agents;
		}
		List<AgentInfo> findAll = agentService.getAgentListOnDB();
		Set<String> ips = new HashSet<String>();
		for (AgentInfo each : findAll) {
			if (each.isApproved()) {
				ips.add(each.getIp());
			}
		}
		Set<AgentIdentity> approvedAgent = new HashSet<AgentIdentity>();
		for (AgentIdentity each : agents) {
			if (ips.contains(((AgentControllerIdentityImplementation) each).getIp())) {
				approvedAgent.add(each);
			}
		}
		return approvedAgent;
	}

	public JavaDataModel getJavaDataModel(AgentIdentity agentIdentity) {
		return agentControllerServer.getJavaDataModel(agentIdentity);
	}

	public SystemDataModel getSystemDataModel(AgentIdentity agentIdentity) {
		return agentControllerServer.getSystemDataModel(agentIdentity);
	}

	/**
	 * Assign agent on given console.
	 * 
	 * @param singleConsole
	 *            {@link SingleConsole} to which agents will be assigend
	 * @param grinderProperties
	 *            {@link GrinderProperties} to be distributed.
	 * @param agentCount
	 *            how much agent are necessary.
	 */
	public synchronized void runAgent(final SingleConsole singleConsole,
					final GrinderProperties grinderProperties, final Integer agentCount) {
		// FIXME : synchronization on this method may have some penalty
		final Set<AgentIdentity> allFreeAgents = agentControllerServer.getAllFreeAgents();

		final Set<AgentIdentity> neccessaryAgents = selectSome(allFreeAgents, agentCount);
		ExecutorService execService = null;
		try {
			// Make the agents connect to console.
			grinderProperties.setInt(GrinderProperties.CONSOLE_PORT, singleConsole.getConsolePort());
			grinderProperties.setProperty(GrinderProperties.CONSOLE_HOST, singleConsole.getConsoleHost());
			execService = ExecutorFactory.createThreadPool("agentStarter", NUMBER_OF_THREAD);
			for (final AgentIdentity eachAgentIdentity : neccessaryAgents) {
				execService.submit(new Runnable() {
					@Override
					public void run() {
						agentControllerServer.startAgent(grinderProperties, eachAgentIdentity);
					}
				});
			}
			execService.awaitTermination(AGENT_RUN_TIMEOUT_SECOND, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new NGrinderRuntimeException("Error while running agent", e);
		} finally {
			if (execService != null) {
				execService.shutdown();
			}
		}
	}

}
