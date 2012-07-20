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
package org.ngrinder.perftest.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.grinder.common.GrinderProperties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.model.BaseModel;

/**
 * Performance Test Entity Use Create user of BaseModel as test owner, use
 * create date of BaseModel as create time, but the created time maybe not the
 * test starting time.
 * 
 */
@Entity
@Table(name = "PERF_TEST")
public class PerfTest extends BaseModel<PerfTest> {

	/**
	 * UUID.
	 */
	private static final long serialVersionUID = 1369809450686098944L;

	@Column(name = "name")
	private String testName;

	@Column(length = 2048)
	private String description;

	@Enumerated(EnumType.STRING)
	private Status status = Status.READY;

	/** The sample interval value, default to 1000ms. */
	private int sampleInterval = 1000;

	/** ignoreSampleCount value, default to 0. */
	private int ignoreSampleCount;

	/** ignoreSampleCount value, default to 0, 0 means collect forever. */
	private int collectSampleCount;

	/** the start time of this test. */
	private Date startTime;

	/** the finish time of this test. */
	private Date finishTime;

	/** the target host to test. */
	@Column(length = 256)
	private String targetHosts;

	/** The send mail code. */
	private boolean sendMail;

	/** The threshold code, R for run count; D for duration. */
	private String threshold;

	// default script name to run test
	private String scriptName;

	private long duration;

	private int runCount;

	private int agentCount;

	private int vuserPerAgent;

	private int processes;

	private int initProcesses;

	private int initSleepTime;

	private int processIncrement;

	private int processIncrementInterval;

	private int threads;

	// followings are test result members
	private int tests = 0;

	private int errors = 0;

	private double meanTestTime = 0;

	private double testTimeStandardDeviation = 0;

	private double tps = 0;

	private double peakTps = 0;

	private int port;

	private int testTrialCount;

	@Transient
	private GrinderProperties grinderProperties;

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	public int getRunCount() {
		return runCount;
	}

	public void setRunCount(int runCount) {
		this.runCount = runCount;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public int getSampleInterval() {
		return sampleInterval;
	}

	public void setSampleInterval(int sampleInterval) {
		this.sampleInterval = sampleInterval;
	}

	public int getIgnoreSampleCount() {
		return ignoreSampleCount;
	}

	public void setIgnoreSampleCount(int ignoreSampleCount) {
		this.ignoreSampleCount = ignoreSampleCount;
	}

	public void setCollectSampleCount(int collectSampleCount) {
		this.collectSampleCount = collectSampleCount;
	}

	public int getCollectSampleCount() {
		return collectSampleCount;
	}

	public String getDescription() {
		return description;
	}

	public String getLastModifiedDateToStr() {
		return DateUtil.formatDate(getLastModifiedDate(), "yyyy-MM-dd  HH:mm:ss");
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTargetHosts() {
		return targetHosts;
	}

	public void setTargetHosts(String theTarget) {
		this.targetHosts = theTarget;
	}

	public boolean isSendMail() {
		return sendMail;
	}

	public void setSendMail(boolean sendMail) {
		this.sendMail = sendMail;
	}

	public String getThreshold() {
		return threshold;
	}

	public void setThreshold(String threshold) {
		this.threshold = threshold;
	}

	public void setGrinderProperties(GrinderProperties properties) {
		this.grinderProperties = properties;
	}

	public GrinderProperties getGrinderProperties() {
		return grinderProperties;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public int getAgentCount() {
		return agentCount;
	}

	public void setAgentCount(int agentCount) {
		this.agentCount = agentCount;
	}

	public int getVuserPerAgent() {
		return vuserPerAgent;
	}

	public void setVuserPerAgent(int vuserPerAgent) {
		this.vuserPerAgent = vuserPerAgent;
	}

	public int getProcesses() {
		return processes;
	}

	public void setProcesses(int processes) {
		this.processes = processes;
	}

	public int getInitProcesses() {
		return initProcesses;
	}

	public void setInitProcesses(int initProcesses) {
		this.initProcesses = initProcesses;
	}

	public int getInitSleepTime() {
		return initSleepTime;
	}

	public void setInitSleepTime(int initSleepTime) {
		this.initSleepTime = initSleepTime;
	}

	public int getProcessIncrement() {
		return processIncrement;
	}

	public void setProcessIncrement(int processIncrement) {
		this.processIncrement = processIncrement;
	}

	public int getProcessIncrementInterval() {
		return processIncrementInterval;
	}

	public void setProcessIncrementInterval(int processIncrementInterval) {
		this.processIncrementInterval = processIncrementInterval;
	}

	/**
	 * @return the threads
	 */
	public int getThreads() {
		return threads;
	}

	/**
	 * @param threads
	 *            the threads to set
	 */
	public void setThreads(int threads) {
		this.threads = threads;
	}


	public int getTests() {
		return tests;
	}

	public void setTests(int tests) {
		this.tests = tests;
	}

	public int getErrors() {
		return errors;
	}

	public void setErrors(int errors) {
		this.errors = errors;
	}

	public Double getMeanTestTime() {
		return meanTestTime;
	}

	public void setMeanTestTime(Double meanTestTime) {
		this.meanTestTime = meanTestTime;
	}

	public Double getTestTimeStandardDeviation() {
		return testTimeStandardDeviation;
	}

	public void setTestTimeStandardDeviation(Double testTimeStandardDeviation) {
		this.testTimeStandardDeviation = testTimeStandardDeviation;
	}

	public Double getTps() {
		return tps;
	}

	public void setTps(Double tps) {
		this.tps = tps;
	}

	public Double getPeakTps() {
		return peakTps;
	}

	public void setPeakTps(Double peakTps) {
		this.peakTps = peakTps;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getTestTrialCount() {
		return testTrialCount;
	}

	public void setTestTrialCount(int testTrialCount) {
		this.testTrialCount = testTrialCount;
	}
}
