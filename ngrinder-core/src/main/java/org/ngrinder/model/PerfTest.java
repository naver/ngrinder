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
package org.ngrinder.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.ngrinder.common.util.DateUtil;

/**
 * Performance Test Entity. Use Create user of BaseModel as test owner, use
 * create date of BaseModel as create time, but the created time maybe not the
 * test starting time.
 * 
 */
@Entity
@Table(name = "PERF_TEST")
public class PerfTest extends BaseModel<PerfTest> {

	private static final long serialVersionUID = 1369809450686098944L;

	private static final int MAX_STRING_SIZE = 2048;

	@Column(name = "name")
	private String testName;

	@Column(length = 2048)
	private String description;

	@Enumerated(EnumType.STRING)
	private Status status = Status.READY;

	/** The sampling Interval value, default to 1000ms. */
	// private Integer sampleInterval = 1000;

	/** ignoreSampleCount value, default to 0. */
	@Column(name = "ignore_sample_count")
	private Integer ignoreSampleCount;

	/** ignoreSampleCount value, default to 0, 0 means collect forever. */
	// private Integer collectSampleCount = 0;

	@Column(name = "scheduled_time")
	@Index(name = "scheduled_time_index")
	/** the scheduled time of this test. */
	private Date scheduledTime;

	@Column(name = "start_time")
	/** the start time of this test. */
	private Date startTime;

	@Column(name = "finish_time")
	/** the finish time of this test. */
	private Date finishTime;

	@Column(name = "target_hosts")
	/** the target host to test. */
	private String targetHosts;

	@Column(name = "send_mail")
	@Type(type = "true_false")
	/** The send mail code. */
	private boolean sendMail;

	@Column(name = "use_rampup")
	@Type(type = "true_false")
	/** Use rampup or not. */
	private boolean useRampUp;

	/** The threshold code, R for run count; D for duration. */
	private String threshold;

	@Column(name = "script_name")
	// default script name to run test
	private String scriptName;

	private Long duration;

	@Column(name = "run_count")
	private Integer runCount = 0;

	@Column(name = "agent_count")
	private Integer agentCount = 0;

	@Column(name = "vuser_per_agent")
	private Integer vuserPerAgent = 0;

	private Integer processes = 0;

	@Column(name = "init_processes")
	private Integer initProcesses = 0;

	@Column(name = "init_sleep_time")
	private Integer initSleepTime = 0;

	@Column(name = "process_increment")
	private Integer processIncrement = 0;

	@Column(name = "process_increment_interval")
	private Integer processIncrementInterval = 0;

	private Integer threads = 0;

	// followings are test result members
	private Integer tests = 0;

	private Integer errors = 0;

	@Column(name = "mean_test_time")
	private Double meanTestTime = 0d;

	@Column(name = "test_time_standard_deviation")
	private Double testTimeStandardDeviation = 0d;

	private Double tps = 0d;

	@Column(name = "peak_tps")
	private Double peakTps = 0d;

	/** Console port for this test. This is the identifier for console */
	private Integer port;

	@Column(name = "test_error_cause")
	@Enumerated(EnumType.STRING)
	private Status testErrorCause = Status.UNKNOWN;

	@Column(name = "grinder_properties")
	@Transient
	private GrinderProperties grinderProperties;

	@Column(name = "distribution_path")
	/** The path used for file distribution */
	private String distributionPath;

	@Column(name = "progress_message", length = MAX_STRING_SIZE)
	private String progressMessage = "";

	@Column(name = "last_progress_message", length = MAX_STRING_SIZE)
	private String lastProgressMessage = "";

	@Column(name = "test_comment", length = MAX_STRING_SIZE)
	private String testComment = "";

	@Column(name = "script_revistion")
	private Long scriptRevision = -1L;

	@Column(name = "stop_request")
	@Type(type = "true_false")
	private Boolean stopRequest = null;

	@Column(name = "date_string")
	@Transient
	private String dateString;

	public String getTestIdentifier() {
		return "perftest_" + getId() + "_" + getLastModifiedUser().getUserId();
	}

	/**
	 * Get total required run count. This is caculated by multiplying
	 * agentcount, threads, processes, runcount.
	 * 
	 * @return runcount
	 */
	public long getTotalRunCount() {
		return getAgentCount() * getThreads() * getProcesses() * (long) getRunCount();
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public Date getScheduledTime() {
		return scheduledTime;
	}

	public void setScheduledTime(Date scheduledTime) {
		this.scheduledTime = scheduledTime;
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

	public Integer getIgnoreSampleCount() {
		return ignoreSampleCount;
	}

	public void setIgnoreSampleCount(Integer ignoreSampleCount) {
		this.ignoreSampleCount = ignoreSampleCount;
	}

	public String getScriptNameInShort() {
		return StringUtils.abbreviate(getScriptName(), 30);
	}

	public String getDescription() {
		return StringUtils.abbreviate(description, 2040);
	}

	public String getLastModifiedDateToStr() {
		return DateUtil.dateToString(getLastModifiedDate());
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTargetHosts() {
		return targetHosts;
	}

	/**
	 * Get ip address of target hosts. if target hosts 'a.com:1.1.1.1' add ip:
	 * '1.1.1.1' if target hosts ':1.1.1.1' add ip: '1.1.1.1' if target hosts
	 * '1.1.1.1' add ip: '1.1.1.1'
	 * 
	 * @return
	 */
	public List<String> getTargetHostIP() {
		List<String> targetIPList = new ArrayList<String>();
		String[] hostsList = StringUtils.split(targetHosts, ",");
		for (String hosts : hostsList) {
			String[] addresses = StringUtils.split(hosts, ":");
			if (addresses.length > 0) {
				targetIPList.add(addresses[addresses.length - 1]);
			} else {
				targetIPList.add(hosts);
			}
		}
		return targetIPList;
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

	public Integer getThreads() {
		return threads;
	}

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

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
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

	public String getDurationStr() {
		return DateUtil.ms2Time(this.duration);
	}

	public String getRuntimeStr() {
		long ms = (this.finishTime == null || this.startTime == null) ? 0 : this.finishTime.getTime() - this.startTime.getTime();
		return DateUtil.ms2Time(ms);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public String getProgressMessage() {
		return StringUtils.defaultIfEmpty(progressMessage, "");
	}

	public void setProgressMessage(String progressMessage) {
		this.progressMessage = StringUtils.defaultIfEmpty(StringUtils.right(progressMessage, MAX_STRING_SIZE), "");
	}

	public Boolean getStopRequest() {
		return stopRequest;
	}

	public void setStopRequest(Boolean stopRequest) {
		this.stopRequest = stopRequest;
	}

	public String getLastProgressMessage() {
		return StringUtils.defaultIfEmpty(lastProgressMessage, "");
	}

	public void clearLstProgressMessage() {
		this.lastProgressMessage = "";
	}

	public void setLastProgressMessage(String lastProgressMessage) {
		if (StringUtils.isEmpty(lastProgressMessage)) {
			return;
		}
		if (!StringUtils.equals(this.lastProgressMessage, lastProgressMessage)) {
			setProgressMessage(getProgressMessage() + this.lastProgressMessage + "\n");
		}
		this.lastProgressMessage = lastProgressMessage;
	}

	public String getTestComment() {
		return testComment;
	}

	public void setTestComment(String testComment) {
		this.testComment = StringUtils.defaultIfEmpty(StringUtils.right(testComment, MAX_STRING_SIZE), "");
	}

	public Long getScriptRevision() {
		return scriptRevision;
	}

	public void setScriptRevision(Long scriptRevision) {
		this.scriptRevision = scriptRevision;
	}

	public String getDateString() {
		return dateString;
	}

	public void setDateString(String dateString) {
		this.dateString = dateString;
	}

	public void clearMessages() {
		clearLstProgressMessage();
		setProgressMessage("");
	}

	public boolean isUseRampUp() {
		return useRampUp;
	}

	public void setUseRampUp(boolean useRampUp) {
		this.useRampUp = useRampUp;
	}

}
