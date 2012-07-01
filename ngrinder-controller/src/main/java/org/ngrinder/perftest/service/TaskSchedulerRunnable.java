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

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.grinder.SingleConsole;
import net.grinder.common.processidentity.AgentIdentity;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.Status;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * task scheduler
 * 
 * @author Liu Zhifei
 * 
 *         2012-3-21
 */
@Service
@Transactional
public class TaskSchedulerRunnable implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(TaskSchedulerRunnable.class);

	@Autowired
	private PerfTestRepository performanceTestRepository;

	@Autowired
	private ConsoleManager consoleManager;

	@Autowired
	private AgentManager agentManager;

	@Override
	public void run() {
		List<PerfTest> runnablePerformanceTests = getRunnablePerformanceTests();
		if (runnablePerformanceTests.isEmpty()) {
			return;
		}
		PerfTest performanceTest = runnablePerformanceTests.get(0);
		performanceTest.setStatus(Status.TESTING);
		performanceTestRepository.save(performanceTest);
		doTest(performanceTest);
	}

	// TODO Auto-generated method stub
	void doTest(PerfTest performanceTest) {
		/**
		 * SingleConsole singleConsole = consoleManager.getAvailableConsole();
		 * BlockingQueue<Agent> agentQueues =
		 * agentManager.getAgents(singleConsole,
		 * performanceTest.getAgentCount()); while(agentQueues.poll(5000,
		 * TimeUnit.MILLISECONDS) != null) { Agent.distributeFiles(); }
		 * 
		 * ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE)
		 */

	}

	@Transactional
	List<PerfTest> getRunnablePerformanceTests() {
		return this.performanceTestRepository.findAllByStatusOrderByCreateDateAsc(Status.READY);
	}

}
