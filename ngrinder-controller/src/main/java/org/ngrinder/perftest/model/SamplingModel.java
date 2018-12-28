package org.ngrinder.perftest.model;

import java.io.Serializable;

/**
 * For modeling perf test running status
 *
 * @since 3.5.0
 */
public class SamplingModel implements Serializable {
	private String runningSample;
	private String agentState;

	public SamplingModel(String runningSample, String agentState) {
		this.runningSample = runningSample;
		this.agentState = agentState;
	}

	public String getRunningSample() {
		return runningSample;
	}

	public String getAgentState() {
		return agentState;
	}
}
