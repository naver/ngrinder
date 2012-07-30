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

import static org.ngrinder.common.constant.NGrinderConstants.MAX_STACKTRACE_STRING_SIZE;

import java.text.ParseException;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.grinder.common.GrinderProperties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ngrinder.common.util.DateUtil;
import org.ngrinder.model.BaseModel;

/**
 * Performance Test Entity Use Create user of BaseModel as test owner, use create date of BaseModel as create time, but
 * the created time maybe not the test starting time.
 * 
 */
@Entity
@Table(name = "PERF_TEST")
public class PerfTest extends BaseModel<PerfTest> {

	/** UUID. */
	private static final long serialVersionUID = 1369809450686098944L;

	@Column(name = "name")
	private String testName;

	@Column(length = 2048)
	private String description;

	@Enumerated(EnumType.STRING)
	private Status status = Status.READY;

	/** The sampling Interval value, default to 1000ms. */
	private Integer sampleInterval = 1000;

	/** ignoreSampleCount value, default to 0. */
	private Integer ignoreSampleCount;

	/** ignoreSampleCount value, default to 0, 0 means collect forever. */
	private Integer collectSampleCount = 0;

	/** the scheduled time of this test. */
	private Date scheduleTime;

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

	private Long duration;

	private Integer runCount;

	private Integer agentCount;

	private Integer vuserPerAgent;

	private Integer processes;

	private Integer initProcesses;

	private Integer initSleepTime;

	private Integer processIncrement;

	private Integer processIncrementInterval;

	private Integer threads;

	// followings are test result members
	private Integer tests = 0;

	private Integer errors = 0;

	private Double meanTestTime = 0d;

	private Double testTimeStandardDeviation = 0d;

	private Double tps = 0d;

	private Double peakTps = 0d;

	/** Console port for this test. This is the identifier for console */
	private Integer port ;

	private Integer testTrialCount;

	@Enumerated(EnumType.STRING)
	private Status testErrorCause;

	@Column(length = MAX_STACKTRACE_STRING_SIZE)
	private String testErrorStackTrace;

	@Transient
	private GrinderProperties grinderProperties;

	/** The path used for file distribution */
	private String distributionPath;

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public Date getScheduleTime() {
		return scheduleTime;
	}

	public void setScheduleTime(Date scheduleTime) {
		this.scheduleTime = scheduleTime;
	}
	
	public void setScheduleInput(String scheduleInput) throws ParseException {
		if (StringUtils.isNotBlank(scheduleInput))
			this.scheduleTime = DateUtil.toSimpleDate(scheduleInput);
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

	public Integer getRunCount() {
		return runCount;
	}

	public void setRunCount(Integer runCount) {
		this.runCount = runCount;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public Integer getSampleInterval() {
		return sampleInterval;
	}

	public void setSampleInterval(Integer sampleInterval) {
		this.sampleInterval = sampleInterval;
	}

	public Integer getIgnoreSampleCount() {
		return ignoreSampleCount;
	}

	public void setIgnoreSampleCount(Integer ignoreSampleCount) {
		this.ignoreSampleCount = ignoreSampleCount;
	}

	public void setCollectSampleCount(Integer collectSampleCount) {
		this.collectSampleCount = collectSampleCount;
	}

	public Integer getCollectSampleCount() {
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

	public Integer getAgentCount() {
		return agentCount;
	}

	public void setAgentCount(Integer agentCount) {
		this.agentCount = agentCount;
	}

	public Integer getVuserPerAgent() {
		return vuserPerAgent;
	}

	public void setVuserPerAgent(Integer vuserPerAgent) {
		this.vuserPerAgent = vuserPerAgent;
	}

	public Integer getProcesses() {
		return processes;
	}

	public void setProcesses(Integer processes) {
		this.processes = processes;
	}

	public Integer getInitProcesses() {
		return initProcesses;
	}

	public void setInitProcesses(Integer initProcesses) {
		this.initProcesses = initProcesses;
	}

	public Integer getInitSleepTime() {
		return initSleepTime;
	}

	public void setInitSleepTime(Integer initSleepTime) {
		this.initSleepTime = initSleepTime;
	}

	public Integer getProcessIncrement() {
		return processIncrement;
	}

	public void setProcessIncrement(Integer processIncrement) {
		this.processIncrement = processIncrement;
	}

	public Integer getProcessIncrementInterval() {
		return processIncrementInterval;
	}

	public void setProcessIncrementInterval(Integer processIncrementInterval) {
		this.processIncrementInterval = processIncrementInterval;
	}

	/**
	 * @return the threads
	 */
	public Integer getThreads() {
		return threads;
	}

	/**
	 * @param threads
	 *            the threads to set
	 */
	public void setThreads(Integer threads) {
		this.threads = threads;
	}

	public Integer getTests() {
		return tests;
	}

	public void setTests(Integer tests) {
		this.tests = tests;
	}

	public Integer getErrors() {
		return errors;
	}

	public void setErrors(Integer errors) {
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

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getTestTrialCount() {
		return testTrialCount;
	}

	public void setTestTrialCount(Integer testTrialCount) {
		this.testTrialCount = testTrialCount;
	}

	public String getTestErrorStackTrace() {
		return testErrorStackTrace;
	}

	public void setTestErrorStackTrace(String testErrorStackTrace) {
		this.testErrorStackTrace = StringUtils.abbreviate(testErrorStackTrace, MAX_STACKTRACE_STRING_SIZE);
	}

	public Status getTestErrorCause() {
		return testErrorCause;
	}

	public void setTestErrorCause(Status errorCause) {
		this.testErrorCause = errorCause;
	}

	public String getDistributionPath() {
		return distributionPath;
	}

	public void setDistributionPath(String distributionPath) {
		this.distributionPath = distributionPath;
	}
}
