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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import net.grinder.AgentControllerServerDaemon;
import net.grinder.SingleConsole;
import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.util.thread.ExecutorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AgentManager {
	public final static Logger logger = LoggerFactory.getLogger(AgentManager.class.getName());
	private final AgentControllerServerDaemon server = new AgentControllerServerDaemon(1011);

	public BlockingQueue<AgentWrapper> getAgentWrappers(SingleConsole singleConsole, final Integer agentCount) {
		final BlockingQueue<AgentWrapper> agentWrappers = new LinkedBlockingQueue<AgentWrapper>();
		ExecutorService execService = ExecutorFactory.createThreadPool("agentRetriever", 1);
		execService.submit(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				for (AgentIdentity agentIdentity : server.getAllAvailableAgents()) {
					// TODO
				}
				return null;
			}
		});
		return agentWrappers;
	}
}
