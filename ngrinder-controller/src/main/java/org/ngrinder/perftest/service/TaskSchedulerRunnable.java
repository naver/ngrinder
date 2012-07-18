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
import java.util.concurrent.TimeUnit;

import net.grinder.SingleConsole;

import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * task scheduler
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
@Transactional
public class TaskSchedulerRunnable {

	private static final Logger LOG = LoggerFactory.getLogger(TaskSchedulerRunnable.class);

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private ConsoleManager consoleManager;

	@Autowired
	private AgentManager agentManager;

	@Scheduled(fixedDelay = 5000)
	public void run() {
		PerfTest runCandidate = perfTestService.getPerfTestCandiate();
		if (runCandidate == null) {
			return;
		}
		doTest(runCandidate);
	}

	public void doTest(PerfTest perfTest) {
		SingleConsole singleConsole = consoleManager.getAvailableConsole();
		if (singleConsole == null) {
			return;
		}
		singleConsole.start();
		perfTest.setStatus(Status.WAITING_AGENT);
		perfTestService.savePerfTest(perfTest);

		BlockingQueue<AgentWrapper> agentQueues = agentManager
				.getAgentWrappers(singleConsole, perfTest.getAgentCount());
		int i = 0;
		while (true) {
			try {
				i++;
				AgentWrapper agentWrapper = agentQueues.poll(1000, TimeUnit.MILLISECONDS);
				agentWrapper.distributeFiles();
			} catch (InterruptedException e) {
				LOG.error("error occurs while distributing files", e);
			}
			if (perfTest.getAgentCount() == i)
				break;
		}

		perfTest.setStatus(Status.TESTING);
		perfTestService.savePerfTest(perfTest);

	}
}
