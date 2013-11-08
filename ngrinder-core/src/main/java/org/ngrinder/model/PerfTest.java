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
import org.ngrinder.common.util.PathUtil;

import com.google.gson.annotations.Expose;

/**
 * Performance Test Entity.
 */
@Entity
@Table(name = "PERF_TEST")
public class PerfTest extends BaseModel<PerfTest> {

	private static final int MARGIN_FOR_ABBREVIATION = 8;

	private static final int MAX_LONG_STRING_SIZE = 2048;

	private static final long serialVersionUID = 1369809450686098944L;

	private static final int MAX_STRING_SIZE = 2048;

	@Expose
	@Column(name = "name")
	private String testName;

	@Expose
	@Column(name = "tag_string")
	private String tagString;

	@Expose
	@Column(length = MAX_LONG_STRING_SIZE)
	private String description;

	@Expose
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private Status status = Status.READY;

	@Expose
	/** ignoreSampleCount value, default to 0. */
	@Column(name = "ignore_sample_count")
	private Integer ignoreSampleCount = 0;

	@Expose
	/** the scheduled time of this test. */
	@Column(name = "scheduled_time")
	@Index(name = "scheduled_time_index")
	private Date scheduledTime;

	@Expose
	/** the start time of this test. */
	@Column(name = "start_time")
	private Date startTime;

	@Expose
	/** the finish time of this test. */
	@Column(name = "finish_time")
	private Date finishTime;

	/**
	 * the target host to test.
	 */
	@Expose
	@Column(name = "target_hosts")
	private String targetHosts;

	/**
	 * The send mail code.
	 */
	@Expose
	@Column(name = "send_mail", columnDefinition = "char(1)")
	@Type(type = "true_false")
	private Boolean sendMail;


	/**
	 * Use rampup or not.
	 */
	@Expose
	@Column(name = "use_rampup", columnDefinition = "char(1)")
	@Type(type = "true_false")
	private Boolean useRampUp = false;

	/**
	 * The threshold code, R for run count; D for duration.
	 */
	@Expose
	@Column(name = "threshold")
	private String threshold;

	@Expose
	@Column(name = "script_name")
	private String scriptName;

	@Expose
	@Column(name = "duration")
	private Long duration;

	@Expose
	@Column(name = "run_count")
	private Integer runCount;

	@Expose
	@Column(name = "agent_count")
	private Integer agentCount;

	@Expose
	@Column(name = "vuser_per_agent")
	private Integer vuserPerAgent;

	@Expose
	@Column(name = "processes")
	private Integer processes;

	@Expose
	@Column(name = "init_processes")
	private Integer initProcesses;

	@Expose
	@Column(name = "init_sleep_time")
	private Integer initSleepTime;

	@Expose
	@Column(name = "process_increment")
	private Integer processIncrement;

	@Expose
	@Column(name = "process_increment_interval")
	private Integer processIncrementInterval;

	@Expose
	@Column(name = "threads")
	private Integer threads;

	// followings are test result members
	@Expose
	@Column(name = "tests")
	private Long tests;

	@Expose
	@Column(name = "errors")
	private Long errors;

	@Expose
	@Column(name = "mean_test_time")
	private Double meanTestTime;

	@Expose
	@Column(name = "test_time_standard_deviation")
	private Double testTimeStandardDeviation;

	@Expose
	@Column(name = "tps")
	private Double tps;

	@Expose
	@Column(name = "peak_tps")
	private Double peakTps;

	/**
	 * Console port for this test. This is the identifier for console
	 */
	@Column(name = "port")
	private Integer port = 0;

	@Expose
	@Column(name = "test_error_cause")
	@Enumerated(EnumType.STRING)
	private Status testErrorCause = Status.UNKNOWN;

	@Column(name = "distribution_path")
	/** The path used for file distribution */
	private String distributionPath;

	@Expose
	@Column(name = "progress_message", length = MAX_STRING_SIZE)
	private String progressMessage = "";

	@Column(name = "last_progress_message", length = MAX_STRING_SIZE)
	private String lastProgressMessage = "";

	@Expose
	@Column(name = "test_comment", length = MAX_STRING_SIZE)
	private String testComment = "";

	@Expose
	@Column(name = "script_revision")
	private Long scriptRevision = -1L;

	@Column(name = "stop_request", columnDefinition = "char(1)")
	@Type(type = "true_false")
	private Boolean stopRequest;

	@Expose
	@Column(name = "region")
	private String region;

	@Column(name = "safe_distribution", columnDefinition = "char(1)")
	@Type(type = "true_false")
	private Boolean safeDistribution = false;

	@Transient
	private String dateString;

	@Transient
	private GrinderProperties grinderProperties;

	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	@JoinTable(name = "PERF_TEST_TAG", /** join column */
			joinColumns = @JoinColumn(name = "perf_test_id"), /** inverse join column */
			inverseJoinColumns = @JoinColumn(name = "tag_id"))
	@Sort(comparator = Tag.class, type = SortType.COMPARATOR)
	private SortedSet<Tag> tags;

	@Column(name = "running_sample", length = 9990)
	private String runningSample;

	@Column(name = "agent_stat", length = 9990)
	private String agentStatus;

	@Column(name = "monitor_stat", length = 2000)
	private String monitorStatus;

	@Expose
	@Column(name = "sampling_interval")
	private Integer samplingInterval;

	@Expose
	@Column(name = "param")
	private String param;


	public String getTestIdentifier() {
		return "perftest_" + getId() + "_" + getLastModifiedUser().getUserId();
	}

	/**
	 * Get total required run count. This is calculated by multiplying agent count, threads,
	 * processes, run count.
	 *
	 * @return run count
	 */
	public long getTotalRunCount() {
		return getAgentCount() * getThreads() * getProcesses() * (long) getRunCount();
	}

	public String getTestName() {
		return testName;
	}

	@Cloneable
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

	@Cloneable
	public void setRunCount(Integer runCount) {
		this.runCount = runCount;
	}

	public Long getDuration() {
		return duration;
	}

	@Cloneable
	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public String getScriptName() {
		return scriptName;
	}

	@Cloneable
	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public Integer getIgnoreSampleCount() {
		return ignoreSampleCount;
	}

	@Cloneable
	public void setIgnoreSampleCount(Integer ignoreSampleCount) {
		this.ignoreSampleCount = ignoreSampleCount;
	}

	public String getScriptNameInShort() {
		return PathUtil.getShortPath(scriptName);
	}

	public String getDescription() {
		return StringUtils.abbreviate(description, MAX_LONG_STRING_SIZE - MARGIN_FOR_ABBREVIATION);
	}

	public String getLastModifiedDateToStr() {
		return DateUtil.dateToString(getLastModifiedDate());
	}

	@Cloneable
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

	@Cloneable
	@ForceMergable
	public void setTargetHosts(String theTarget) {
		this.targetHosts = theTarget;
	}

	public String getThreshold() {
		return threshold;
	}

	public boolean isThresholdDuration() {
		return "D".equals(getThreshold());
	}

	public boolean isThresholdRunCount() {
		return "R".equals(getThreshold());
	}


	@Cloneable
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


	@Cloneable
	public void setAgentCount(Integer agentCount) {
		this.agentCount = agentCount;
	}

	public Integer getVuserPerAgent() {
		return vuserPerAgent;
	}


	@Cloneable
	public void setVuserPerAgent(Integer vuserPerAgent) {
		this.vuserPerAgent = vuserPerAgent;
	}

	public Integer getProcesses() {
		return processes;
	}


	@Cloneable
	public void setProcesses(Integer processes) {
		this.processes = processes;
	}

	public Integer getInitProcesses() {
		return initProcesses;
	}


	@Cloneable
	public void setInitProcesses(Integer initProcesses) {
		this.initProcesses = initProcesses;
	}

	public Integer getInitSleepTime() {
		return initSleepTime;
	}


	@Cloneable
	public void setInitSleepTime(Integer initSleepTime) {
		this.initSleepTime = initSleepTime;
	}

	public Integer getProcessIncrement() {
		return processIncrement;
	}


	@Cloneable
	public void setProcessIncrement(Integer processIncrement) {
		this.processIncrement = processIncrement;
	}

	public Integer getProcessIncrementInterval() {
		return processIncrementInterval;
	}


	@Cloneable
	public void setProcessIncrementInterval(Integer processIncrementInterval) {
		this.processIncrementInterval = processIncrementInterval;
	}

	public Integer getThreads() {
		return threads;
	}


	@Cloneable
	public void setThreads(Integer threads) {
		this.threads = threads;
	}

	public Long getTests() {
		return tests;
	}

	public void setTests(Long tests) {
		this.tests = tests;
	}

	public Long getErrors() {
		return errors;
	}

	public void setErrors(Long errors) {
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

	/**
	 * Get Duration time in HH:MM:SS style.
	 *
	 * @return formatted duration string
	 */
	public String getDurationStr() {
		return DateUtil.ms2Time(this.duration);
	}


	/**
	 * Get Running time in HH:MM:SS style.
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
	 * @param lastProgressMessage message
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


	@Cloneable
	public void setUseRampUp(Boolean useRampUp) {
		this.useRampUp = useRampUp;
	}

	public Boolean getSendMail() {
		return sendMail;
	}


	@Cloneable
	public void setSendMail(Boolean sendMail) {
		this.sendMail = sendMail;
	}

	public String getTagString() {
		return tagString;
	}


	@Cloneable
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


	@Cloneable
	public void setRegion(String region) {
		this.region = region;
	}

	public Boolean getSafeDistribution() {
		return safeDistribution == null ? false : safeDistribution;
	}


	@Cloneable
	public void setSafeDistribution(Boolean safeDistribution) {
		this.safeDistribution = safeDistribution;
	}

	public String getRunningSample() {
		return runningSample;
	}


	@ForceMergable
	public void setRunningSample(String runningSample) {
		this.runningSample = runningSample;
	}

	public String getAgentStatus() {
		return agentStatus;
	}

	@ForceMergable
	public void setAgentStatus(String agentStatus) {
		this.agentStatus = agentStatus;
	}

	public String getMonitorStatus() {
		return monitorStatus;
	}

	@ForceMergable
	public void setMonitorStatus(String monitorStatus) {
		this.monitorStatus = monitorStatus;
	}

	public Integer getSamplingInterval() {
		return samplingInterval;
	}


	@Cloneable
	public void setSamplingInterval(Integer samplingInterval) {
		this.samplingInterval = samplingInterval;
	}

	public String getParam() {
		return param;
	}

	@ForceMergable
	@Cloneable
	public void setParam(String param) {
		this.param = param;
	}


}
