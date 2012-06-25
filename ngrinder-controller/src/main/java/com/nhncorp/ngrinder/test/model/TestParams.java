package com.nhncorp.ngrinder.test.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.nhncorp.ngrinder.core.model.BaseEntity;

/**
 * test params entity
 * 
 * @author Liu Zhifei
 * @date 2012-6-13
 */
@Entity
@Table(name = "TEST_PARAMS")
public class TestParams extends BaseEntity {

	private static final long serialVersionUID = 83379827758499899L;

	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "SCRIPT_ID")
	// private transient Script script;
	//
	// @Column(name = "SCRIPT_ID", nullable = false)
	// private long scriptId;

	@Column(name = "VUSERS")
	private int vusers;

	@Column(name = "RUN_DURATION")
	private long runDuration;

	@Column(name = "RUN_COUNT")
	private long runCount;

	@Column(name = "SAMPLE_INTERVAL")
	private long sampleInterval;

	@Column(name = "IGNORE_COUNT")
	private long ignoreCount;

	@Column(name = "MONITOR")
	private boolean monitor = false;

	/**
	 * 0: run only once <br>
	 * !0: always run
	 */
	@Column(name = "RUN_INTERVAL")
	private long runInterval;

	// other...

	public int getVusers() {
		return vusers;
	}

	public void setVusers(int vusers) {
		this.vusers = vusers;
	}

	public long getRunDuration() {
		return runDuration;
	}

	public void setRunDuration(long runDuration) {
		this.runDuration = runDuration;
	}

	public long getRunCount() {
		return runCount;
	}

	public void setRunCount(long runCount) {
		this.runCount = runCount;
	}

	public long getSampleInterval() {
		return sampleInterval;
	}

	public void setSampleInterval(long sampleInterval) {
		this.sampleInterval = sampleInterval;
	}

	public long getIgnoreCount() {
		return ignoreCount;
	}

	public void setIgnoreCount(long ignoreCount) {
		this.ignoreCount = ignoreCount;
	}

	public boolean isMonitor() {
		return monitor;
	}

	public void setMonitor(boolean monitor) {
		this.monitor = monitor;
	}

	public long getRunInterval() {
		return runInterval;
	}

	public void setRunInterval(long runInterval) {
		this.runInterval = runInterval;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
