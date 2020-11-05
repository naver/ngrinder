package org.ngrinder.perftest.model;

import lombok.Getter;

import java.io.Serializable;

/**
 * For modeling perf test running status
 *
 * @since 3.5.0
 */
@Getter
public class SamplingModel implements Serializable {
	private final String runningSample;
	private final String agentState;

	public SamplingModel(String runningSample, String agentState) {
		this.runningSample = runningSample;
		this.agentState = agentState;
	}
}
