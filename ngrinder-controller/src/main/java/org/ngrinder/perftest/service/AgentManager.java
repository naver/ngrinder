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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.agent.model.AgentInfo;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.User;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Agent manager.
 * 
 * This class has {@link AgentControllerServerDaemon} internally and manage to
 * the agent connection.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
public class AgentManager implements NGrinderConstants {
	public static final Logger LOGGER = LoggerFactory.getLogger(AgentManager.class);
	private AgentControllerServerDaemon agentControllerServer;
	private static final int NUMBER_OF_THREAD = 3;
	private static final int AGENT_RUN_TIMEOUT_SECOND = 10;

	@Autowired
	private Config config;

	@Autowired
	private AgentManagerService agentService;

	/**
	 * Initialize agent manager.
	 */
	@PostConstruct
	public void init() {
		agentControllerServer = new AgentControllerServerDaemon(
				AgentControllerCommunicationDefauts.DEFAULT_AGENT_CONTROLLER_SERVER_PORT);
		agentControllerServer.start();
		agentControllerServer.addLogArrivedListener(new LogArrivedListener() {
			@Override
			public void logArrived(String testId, AgentAddress agentAddress, byte[] logs) {
				AgentControllerIdentityImplementation agentIdentity = convert(agentAddress.getIdentity());
				if (ArrayUtils.isEmpty(logs)) {
					LOGGER.error("Log is arrived from {} but no log content", agentIdentity.getIp());
				}
				File logFile = null;
				try {
					logFile = new File(config.getHome().getPerfTestLogDirectory(testId.replace("test_", "")),
							agentIdentity.getName() + "-" + agentIdentity.getRegion() + "-log.zip");
					FileUtils.writeByteArrayToFile(logFile, logs);
				} catch (IOException e) {
					LOGGER.error("Error while write logs from {} to {}", agentAddress.getIdentity().getName(),
							logFile.getAbsolutePath());
					LOGGER.error("Error is following", e);
				}
			}
		});
	}

	/**
	 * Shutdown agent controller server.
	 */
	@PreDestroy
	public void destroy() {
		agentControllerServer.shutdown();
	}

	/**
	 * 
	 * Get the port which given agent is connecting to.
	 * 
	 * @param agentIdentity
	 *            agent identity
	 * @return port
	 */
	public int getAgentConnectingPort(AgentIdentity agentIdentity) {
		return agentControllerServer.getAgentConnectingPort(agentIdentity);
	}

	/**
	 * Get the agent status of the given agent.
	 * 
	 * @param agentIdentity
	 *            agentIdentity of one agent
	 * @return status agent controller status of one agent
	 */
	public AgentControllerState getAgentState(AgentIdentity agentIdentity) {
		return agentControllerServer.getAgentState(agentIdentity);
	}

	/**
	 * Get all agents which is connected to agent controller.
	 * 
	 * @return agents set
	 */
	public Set<AgentIdentity> getAllAttachedAgents() {
		return agentControllerServer.getAllAvailableAgents();
	}

	/**
	 * Get the max agent size per console.
	 * 
	 * @return max agent size per console
	 */
	public int getMaxAgentSizePerConsole() {
		return config.getSystemProperties().getPropertyInt("agent.max.size",
				NGrinderConstants.MAX_AGENT_SIZE_PER_CONSOLE);
	}

	/**
	 * Get the max vuser per agent.
	 * 
	 * @return max vuser per agent
	 */
	public int getMaxVuserPerAgent() {
		return config.getSystemProperties().getPropertyInt("agent.max.vuser", NGrinderConstants.MAX_VUSER_PER_AGENT);
	}

	/**
	 * Get the max run count per thread.
	 * 
	 * @return max run count per thread
	 */
	public int getMaxRunCount() {
		return config.getSystemProperties().getPropertyInt("agent.max.runcount", NGrinderConstants.MAX_RUN_COUNT);
	}

	/**
	 * Get the max run hour.
	 * 
	 * @return max run hour
	 */
	public int getMaxRunHour() {
		return config.getSystemProperties().getPropertyInt("agent.max.runhour", NGrinderConstants.MAX_RUN_HOUR);
	}

	/**
	 * Get the {@link AgentIdentity} which has the given ip.
	 * 
	 * @param agentIP
	 *            agent ip
	 * @return {@link AgentControllerIdentityImplementation}
	 */
	public AgentControllerIdentityImplementation getAgentIdentityByIp(String agentIP) {
		for (AgentIdentity agentIdentity : getAllAttachedAgents()) {
			if (StringUtils.equals(convert(agentIdentity).getIp(), agentIP)) {
				return convert(agentIdentity);
			}
		}
		return null;
	}

	/**
	 * Convert {@link AgentIdentity} to
	 * {@link AgentControllerIdentityImplementation} type.
	 * 
	 * @param identity
	 *            identity
	 * @return convertity identity.
	 */
	AgentControllerIdentityImplementation convert(AgentIdentity identity) {
		return (AgentControllerIdentityImplementation) identity;
	}

	/**
	 * Get all agents which are not used now.
	 * 
	 * @return AgentIndentity set
	 */
	public Set<AgentIdentity> getAllFreeAgents() {
		return agentControllerServer.getAllFreeAgents();
	}

	/**
	 * Get the all approved agents for given user which are not used now.
	 * 
	 * @param user
	 *            user
	 * @return AgentIndentity set
	 */
	public Set<AgentIdentity> getAllFreeApprovedAgentsForUser(User user) {
		return filterUserAgents(getAllFreeApprovedAgents(), user.getUserId());
	}

	/**
	 * Get the all approved agents which are not used now.
	 * 
	 * @return AgentIndentity set
	 */
	public Set<AgentIdentity> getAllFreeApprovedAgents() {
		Set<AgentIdentity> allFreeAgents = agentControllerServer.getAllFreeAgents();
		return filterApprovedAgents(allFreeAgents);
	}

	/**
	 * Get the all approved agents for given user.
	 * 
	 * @param user
	 *            user
	 * @return AgentIndentity set
	 */
	public Set<AgentIdentity> getAllApprovedAgents(User user) {
		return filterUserAgents(getAllApprovedAgents(), user.getUserId());
	}

	/**
	 * Get the all shared approved agents.
	 * 
	 * @return AgentIndentity set
	 */
	public Set<AgentIdentity> getAllSharedAgents() {
		return filterSharedAgents(getAllApprovedAgents());
	}

	/**
	 * Get the all approved agents.
	 * 
	 * @return AgentIndentity set
	 */
	public Set<AgentIdentity> getAllApprovedAgents() {
		Set<AgentIdentity> allAgents = agentControllerServer.getAllAvailableAgents();
		return filterApprovedAgents(allAgents);
	}

	/**
	 * Filter the approved agents from given agents.
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

	/**
	 * Filter the shared agents from given agents.
	 * 
	 * 
	 * @param agents
	 *            all agents
	 * @return userOwned agents.
	 */
	public Set<AgentIdentity> filterSharedAgents(Set<AgentIdentity> agents) {

		Set<AgentIdentity> userAgent = new HashSet<AgentIdentity>();
		for (AgentIdentity each : agents) {
			String region = ((AgentControllerIdentityImplementation) each).getRegion();

			if (StringUtils.containsNone(region, "owned_")) {
				userAgent.add(each);
			}
		}
		return userAgent;
	}

	/**
	 * Filter the user owned agents from given agents.
	 * 
	 * 
	 * @param agents
	 *            all agents
	 * @param userId
	 *            userId
	 * @return userOwned agents.
	 */
	public Set<AgentIdentity> filterUserAgents(Set<AgentIdentity> agents, String userId) {

		Set<AgentIdentity> userAgent = new HashSet<AgentIdentity>();
		for (AgentIdentity each : agents) {
			String region = ((AgentControllerIdentityImplementation) each).getRegion();

			if (StringUtils.endsWith(region, "owned_" + userId) || StringUtils.containsNone(region, "owned_")) {
				userAgent.add(each);
			}
		}
		return userAgent;
	}

	/**
	 * Get the current system performance of the given agent.
	 * 
	 * @param agentIdentity
	 *            {@link AgentIdentity}
	 * @return {@link SystemDataModel} instance.
	 */
	public SystemDataModel getSystemDataModel(AgentIdentity agentIdentity) {
		return agentControllerServer.getSystemDataModel(agentIdentity);
	}

	/**
	 * Assign the agents on the given console.
	 * 
	 * @param user
	 *            user
	 * @param singleConsole
	 *            {@link SingleConsole} to which agents will be assigned
	 * @param grinderProperties
	 *            {@link GrinderProperties} to be distributed.
	 * @param agentCount
	 *            the count of agents.
	 */
	public synchronized void runAgent(User user, final SingleConsole singleConsole,
			final GrinderProperties grinderProperties, final Integer agentCount) {
		final Set<AgentIdentity> allFreeAgents = getAllFreeApprovedAgentsForUser(user);
		final Set<AgentIdentity> neccessaryAgents = selectAgent(user, allFreeAgents, agentCount);
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
			throw new NGrinderRuntimeException("Error while running agent. Starting agent is interrupted.", e);
		} finally {
			if (execService != null) {
				execService.shutdown();
			}
		}
	}

	/**
	 * Select agent. This method return agent set which is belong to the given
	 * user first and then share agent set.
	 * 
	 * @param user
	 *            user
	 * @param allFreeAgents
	 *            agents
	 * @param agentCount
	 *            number of agent
	 * @return selected agent.
	 */
	public Set<AgentIdentity> selectAgent(User user, Set<AgentIdentity> allFreeAgents, int agentCount) {
		Set<AgentIdentity> userAgent = new HashSet<AgentIdentity>();
		for (AgentIdentity each : allFreeAgents) {
			String region = ((AgentControllerIdentityImplementation) each).getRegion();
			if (StringUtils.endsWith(region, "owned_" + user.getUserId())) {
				userAgent.add(each);
				if (userAgent.size() == agentCount) {
					return userAgent;
				}
			}
		}

		for (AgentIdentity each : allFreeAgents) {
			String region = ((AgentControllerIdentityImplementation) each).getRegion();
			if (StringUtils.containsNone(region, "owned_")) {
				userAgent.add(each);
				if (userAgent.size() == agentCount) {
					return userAgent;
				}
			}
		}
		return userAgent;
	}

	/**
	 * Stop agent in force.
	 * 
	 * @param agentIdentity
	 *            agent identity
	 */
	public void stopAgent(AgentIdentity agentIdentity) {
		agentControllerServer.stopAgent(agentIdentity);
	}

	/**
	 * Stop agents which uses the given console port  in force.
	 * 
	 * @param consolePort console port.
	 */
	public void stopAgent(int consolePort) {
		for (AgentIdentity each : agentControllerServer.getAllAvailableAgents()) {
			int agentConnectingPort = agentControllerServer.getAgentConnectingPort(each);
			if (agentConnectingPort == consolePort
					&& agentControllerServer.getAgentState(each) == AgentControllerState.BUSY) {
				agentControllerServer.stopAgent(each);
			}
		}
	}
}
