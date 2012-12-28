/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.grinder.common.GrinderProperties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.ngrinder.common.util.DateUtil;

/**
 * Performance Test Entity. <br/>
 * 
 */
@Entity
@Table(name = "PERF_TEST")
public class PerfTest extends BaseModel<PerfTest> {

	private static final int MARGIN_FOR_ABBREVIATATION = 8;

	private static final int MAX_SHORT_STRING_SIZE = 30;

	private static final int MAX_LONG_STRING_SIZE = 2048;

	private static final long serialVersionUID = 1369809450686098944L;

	private static final int MAX_STRING_SIZE = 2048;

	@Column(name = "name")
	private String testName;

	@Column(name = "tag_string")
	private String tagString;

	@Column(length = MAX_LONG_STRING_SIZE)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private Status status = Status.READY;

	/** ignoreSampleCount value, default to 0. */
	@Column(name = "ignore_sample_count")
	private Integer ignoreSampleCount = 0;

	/** the scheduled time of this test. */
	@Column(name = "scheduled_time")
	@Index(name = "scheduled_time_index")
	private Date scheduledTime;

	/** the start time of this test. */
	@Column(name = "start_time")
	private Date startTime;

	/** the finish time of this test. */
	@Column(name = "finish_time")
	private Date finishTime;

	/** the target host to test. */
	@Column(name = "target_hosts")
	private String targetHosts;

	/** The send mail code. */
	@Column(name = "send_mail", columnDefinition = "char(1)")
	@Type(type = "true_false")
	private Boolean sendMail;

	/** Use rampup or not. */
	@Column(name = "use_rampup", columnDefinition = "char(1)")
	@Type(type = "true_false")
	private Boolean useRampUp = false;

	/** The threshold code, R for run count; D for duration. */
	@Column(name = "threshold")
	private String threshold;

	@Column(name = "script_name")
	private String scriptName;

	@Column(name = "duration")
	private Long duration;

	@Column(name = "run_count")
	private Integer runCount;

	@Column(name = "agent_count")
	private Integer agentCount;

	@Column(name = "vuser_per_agent")
	private Integer vuserPerAgent;

	@Column(name = "processes")
	private Integer processes;

	@Column(name = "init_processes")
	private Integer initProcesses;

	@Column(name = "init_sleep_time")
	private Integer initSleepTime;

	@Column(name = "process_increment")
	private Integer processIncrement;

	@Column(name = "process_increment_interval")
	private Integer processIncrementInterval;

	@Column(name = "threads")
	private Integer threads;

	// followings are test result members
	@Column(name = "tests")
	private Integer tests;

	@Column(name = "errors")
	private Integer errors;

	@Column(name = "mean_test_time")
	private Double meanTestTime;

	@Column(name = "test_time_standard_deviation")
	private Double testTimeStandardDeviation;

	@Column(name = "tps")
	private Double tps;

	@Column(name = "peak_tps")
	private Double peakTps;

	/** Console port for this test. This is the identifier for console */
	@Column(name = "port")
	private Integer port = 0;

	@Column(name = "test_error_cause")
	@Enumerated(EnumType.STRING)
	private Status testErrorCause = Status.UNKNOWN;

	@Column(name = "distribution_path")
	/** The path used for file distribution */
	private String distributionPath;

	@Column(name = "progress_message", length = MAX_STRING_SIZE)
	private String progressMessage = "";

	@Column(name = "last_progress_message", length = MAX_STRING_SIZE)
	private String lastProgressMessage = "";

	@Column(name = "test_comment", length = MAX_STRING_SIZE)
	private String testComment = "";

	@Column(name = "script_revision")
	private Long scriptRevision = -1L;

	@Column(name = "stop_request", columnDefinition = "char(1)")
	@Type(type = "true_false")
	private Boolean stopRequest;

	@Column(name = "region")
	private String region;

	@Transient
	private String dateString;

	@Transient
	private GrinderProperties grinderProperties;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
	@JoinTable(
			name = "PERF_TEST_TAG", joinColumns = @JoinColumn(name = "perf_test_id"), 
			inverseJoinColumns = @JoinColumn(name = "tag_id"))
	@Sort(comparator = Tag.class, type = SortType.COMPARATOR)
	private SortedSet<Tag> tags;

	public String getTestIdentifier() {
		return "perftest_" + getId() + "_" + getLastModifiedUser().getUserId();
	}

	/**
	 * Get total required run count. This is calculated by multiplying agentcount, threads,
	 * processes, runcount.
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
		return StringUtils.abbreviate(getScriptName(), MAX_SHORT_STRING_SIZE);
	}

	public String getDescription() {
		return StringUtils.abbreviate(description, MAX_LONG_STRING_SIZE - MARGIN_FOR_ABBREVIATATION);
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
	 * Get ip address of target hosts. if target hosts 'a.com:1.1.1.1' add ip: '1.1.1.1' if target
	 * hosts ':1.1.1.1' add ip: '1.1.1.1' if target hosts '1.1.1.1' add ip: '1.1.1.1'
	 * 
	 * @return host ip list
	 */
	public List<String> getTargetHostIP() {
		List<String> targetIPList = new ArrayList<String>();
		String[] hostsList = StringUtils.split(StringUtils.trimToEmpty(targetHosts), ",");
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

	@ForceMergable
	public void setTargetHosts(String theTarget) {
		this.targetHosts = theTarget;
	}

	public String getThreshold() {
		return threshold;
	}

	public boolean isThreshholdDuration() {
		return "D".equals(getThreshold());
	}

	public boolean isThreshholdRunCount() {
		return "R".equals(getThreshold());
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

	/**
	 * Get Runtime in HH:MM:SS style.
	 * 
	 * @return formatted runtime string
	 */
	public String getRuntimeStr() {
		long ms = (this.finishTime == null || this.startTime == null) ? 0 : this.finishTime.getTime()
						- this.startTime.getTime();
		return DateUtil.ms2Time(ms);
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toStringExclude(this, "tags");
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

	/**
	 * Clear the last progress message.
	 */
	public void clearLastProgressMessage() {
		this.lastProgressMessage = "";
	}

	/**
	 * Set the last progress message.
	 * 
	 * @param lastProgressMessage
	 *            message
	 */
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
		this.testComment = StringUtils.trimToEmpty(StringUtils.right(testComment, MAX_STRING_SIZE));
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

	/**
	 * Clear all messages.
	 */
	public void clearMessages() {
		clearLastProgressMessage();
		setProgressMessage("");
	}

	public Boolean getUseRampUp() {
		return useRampUp;
	}

	public void setUseRampUp(Boolean useRampUp) {
		this.useRampUp = useRampUp;
	}

	public Boolean getSendMail() {
		return sendMail;
	}

	public void setSendMail(Boolean sendMail) {
		this.sendMail = sendMail;
	}

	public String getTagString() {
		return tagString;
	}

	@ForceMergable
	public void setTagString(String tagString) {
		this.tagString = tagString;
	}

	public SortedSet<Tag> getTags() {
		return tags;
	}

	public void setTags(SortedSet<Tag> tags) {
		this.tags = tags;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}
}
