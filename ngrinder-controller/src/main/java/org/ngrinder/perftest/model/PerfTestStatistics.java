package org.ngrinder.perftest.model;

import org.ngrinder.model.PerfTest;
import org.ngrinder.model.User;

/**
 * Current perftest staticstics model
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class PerfTestStatistics {
	private User user;
	private int agentCount;
	private int testCount;

	public PerfTestStatistics(User user) {
		this.user = user;
	}

	public void addPerfTest(PerfTest perfTest) {
		testCount++;
		agentCount += perfTest.getAgentCount();
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getAgentCount() {
		return agentCount;
	}

	public void setAgentCount(int agentCount) {
		this.agentCount = agentCount;
	}

	public int getTestCount() {
		return testCount;
	}

	public void setTestCount(int testCount) {
		this.testCount = testCount;
	}
}
