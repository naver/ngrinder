package net.grinder.performance;

import java.io.Serializable;

public class Performance implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final long cpu;
	private final long mem;

	public Performance(long cpu, long mem) {
		this.cpu = cpu;
		this.mem = mem;
	}

	public long getCpu() {
		return cpu;
	}

	public long getMem() {
		return mem;
	}
}
