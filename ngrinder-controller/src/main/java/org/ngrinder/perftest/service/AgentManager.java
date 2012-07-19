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

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import net.grinder.AgentControllerServerDaemon;
import net.grinder.SingleConsole;
import net.grinder.common.GrinderProperties;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.util.thread.ExecutorFactory;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Agent manager class.
 * 
 * This class runs {@link AgentControllerServerDaemon} internally and manage to
 * agent connection.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
public class AgentManager {
	public static final Logger LOGGER = LoggerFactory.getLogger(AgentManager.class.getName());
	private final AgentControllerServerDaemon server = new AgentControllerServerDaemon(1011);
	private static final int NUMBER_OF_THREAD = 3;
	private static final int AGENT_RUN_TIMEOUT_SECOND = 10;

	/**
	 * Initialize agent manager.
	 */
	@PostConstruct
	public void init() {
		server.start();
	}

	/**
	 * Assign agent on given console
	 * 
	 * @param singleConsole
	 * @param grinderProperties
	 * @param agentCount
	 *            how much agent are necessary.
	 */
	public synchronized void runAgent(final SingleConsole singleConsole, final GrinderProperties grinderProperties,
			final Integer agentCount) {
		// FIXME : synchronization on this method may have some penalty
		final Set<AgentIdentity> allFreeAgents = server.getAllFreeAgents();

		final Set<AgentIdentity> neccessaryAgents = selectSome(allFreeAgents, agentCount);
		try {
			// Make the agents connect to console.
			grinderProperties.setInt(GrinderProperties.CONSOLE_PORT, singleConsole.getConsolePort());
			ExecutorService execService = ExecutorFactory.createThreadPool("agentStarter", NUMBER_OF_THREAD);
			for (final AgentIdentity eachAgentIdentity : neccessaryAgents) {
				execService.submit(new Runnable() {
					@Override
					public void run() {
						server.startAgent(grinderProperties, eachAgentIdentity);
					}
				});
			}
			execService.awaitTermination(AGENT_RUN_TIMEOUT_SECOND, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			LOGGER.error("Error while running agent", e);
		}
	}
}
