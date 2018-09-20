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

import com.google.gson.annotations.Expose;
import net.grinder.common.GrinderProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.ngrinder.common.util.DateUtils;
import org.ngrinder.common.util.PathUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import static org.ngrinder.common.util.AccessUtils.getSafe;

/**
 * Performance Test Entity.
 */

@SuppressWarnings({"JpaDataSourceORMInspection", "UnusedDeclaration", "JpaAttributeTypeInspection"})
@Entity
@Table(name = "PERF_TEST")
public class PerfTest extends BaseModel<PerfTest> {

	private static final int MARGIN_FOR_ABBREVIATION = 8;

	private static final int MAX_LONG_STRING_SIZE = 2048;

	private static final long serialVersionUID = 1369809450686098944L;

	private static final int MAX_STRING_SIZE = 2048;

	public PerfTest() {

	}

	/**
	 * Constructor.
	 *
	 * @param createdUser crested user.
	 */
	public PerfTest(User createdUser) {
		this.setCreatedUser(createdUser);
	}

	@Expose
	@Cloneable
	@Column(name = "name")
	private String testName;

	@Expose
	@Cloneable
	@Column(name = "tag_string")
	private String tagString;

	@Expose
	@Cloneable
	@Column(length = MAX_LONG_STRING_SIZE)
	private String description;

	@Expose
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private Status status;

	@Expose
	@Cloneable
	/** ignoreSampleCount value, default to 0. */
	@Column(name = "ignore_sample_count")
	private Integer ignoreSampleCount;

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
	@Cloneable
	@Column(name = "target_hosts", length = 65535)
	private String targetHosts;

	/**
	 * The send mail code.
	 */
	@Expose
	@Cloneable
	@Column(name = "send_mail", columnDefinition = "char(1)")
	@Type(type = "true_false")
	private Boolean sendMail;


	/**
	 * Use rampUp or not.
	 */
	@Expose
	@Cloneable
	@Column(name = "use_rampup", columnDefinition = "char(1)")
	@Type(type = "true_false")
	private Boolean useRampUp;

	public RampUp getRampUpType() {
		return rampUpType;
	}

	public void setRampUpType(RampUp rampUpType) {
		this.rampUpType = rampUpType;
	}

	/**
	 * Use rampUp or not.
	 */
	@Expose
	@Cloneable
	@Column(name = "ramp_up_type")
	@Enumerated(EnumType.STRING)
	private RampUp rampUpType;


	/**
	 * The threshold code, R for run count; D for duration.
	 */
	@Expose
	@Cloneable
	@Column(name = "threshold")
	private String threshold;

	@Expose
	@Cloneable
	@Column(name = "script_name")
	private String scriptName;

	@Expose
	@Cloneable
	@Column(name = "duration")
	private Long duration;

	@Expose
	@Cloneable
	@Column(name = "run_count")
	private Integer runCount;

	@Expose
	@Cloneable
	@Column(name = "agent_count")
	private Integer agentCount;

	@Expose
	@Cloneable
	@Column(name = "vuser_per_agent")
	private Integer vuserPerAgent;

	@Expose
	@Cloneable
	@Column(name = "processes")
	private Integer processes;

	@Expose
	@Cloneable
	@Column(name = "ramp_up_init_count")
	private Integer rampUpInitCount;

	@Expose
	@Cloneable
	@Column(name = "ramp_up_init_sleep_time")
	private Integer rampUpInitSleepTime;

	@Expose
	@Cloneable
	@Column(name = "ramp_up_step")
	private Integer rampUpStep;

	@Expose
	@Cloneable
	@Column(name = "ramp_up_increment_interval")
	private Integer rampUpIncrementInterval;

	@Expose
	@Cloneable
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
	private Integer port;

	@Expose
	@Column(name = "test_error_cause")
	@Enumerated(EnumType.STRING)
	private Status testErrorCause;

	@Column(name = "distribution_path")
	/** The path used for file distribution */
	private String distributionPath;

	@Expose
	@Column(name = "progress_message", length = MAX_STRING_SIZE)
	private String progressMessage;

	@Column(name = "last_progress_message", length = MAX_STRING_SIZE)
	private String lastProgressMessage;

	@Expose
	@Column(name = "test_comment", length = MAX_STRING_SIZE)
	private String testComment;

	@Expose
	@Column(name = "script_revision")
	private Long scriptRevision;

	@Expose
	@Column(name = "stop_request")
	@Type(type = "true_false")
	private Boolean stopRequest;

	@Expose
	@Cloneable
	@Column(name = "region")
	private String region;

	@Column(name = "safe_distribution")
	@Cloneable
	@Type(type = "true_false")
	private Boolean safeDistribution;

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
	private String agentState;

	@Column(name = "monitor_stat", length = 2000)
	private String monitorState;

	@Expose
	@Cloneable
	@Column(name = "sampling_interval")
	private Integer samplingInterval;

	@Expose
	@Cloneable
	@Column(name = "param")
	private String param;

	@PrePersist
	@PreUpdate
	public void init() {
		this.status = getSafe(this.status, Status.SAVED);
		this.agentCount = getSafe(this.agentCount);
		this.port = getSafe(this.port);
		this.processes = getSafe(this.processes, 1);
		this.threads = getSafe(this.threads, 1);
		this.scriptName = getSafe(this.scriptName, "");
		this.testName = getSafe(this.testName, "");
		this.progressMessage = getSafe(this.progressMessage, "");
		this.lastProgressMessage = getSafe(this.lastProgressMessage, "");
		this.testComment = getSafe(this.testComment, "");
		this.threshold = getSafe(this.threshold, "D");
		if (isThresholdRunCount()) {
			this.setIgnoreSampleCount(0);
		} else {
			this.ignoreSampleCount = getSafe(this.ignoreSampleCount);
		}
		this.runCount = getSafe(this.runCount);
		this.duration = getSafe(this.duration, 60000L);
		this.samplingInterval = getSafe(this.samplingInterval, 2);
		this.scriptRevision = getSafe(this.scriptRevision, -1L);
		this.param = getSafe(this.param, "");
		this.region = getSafe(this.region, "NONE");
		this.targetHosts = getSafe(this.targetHosts, "");
		this.description = getSafe(this.description, "");
		this.tagString = getSafe(this.tagString, "");
		this.vuserPerAgent = getSafe(this.vuserPerAgent, 1);
		this.safeDistribution = getSafe(this.safeDistribution, false);
		this.useRampUp = getSafe(this.useRampUp, false);
		this.rampUpInitCount = getSafe(this.rampUpInitCount, 0);
		this.rampUpStep = getSafe(this.rampUpStep, 1);
		this.rampUpInitSleepTime = getSafe(this.rampUpInitSleepTime, 0);
		this.rampUpIncrementInterval = getSafe(this.rampUpIncrementInterval, 1000);
		this.rampUpType = getSafe(this.rampUpType, RampUp.PROCESS);
	}


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
		return PathUtils.getShortPath(scriptName);
	}

	public String getDescription() {
		return StringUtils.abbreviate(description, MAX_LONG_STRING_SIZE - MARGIN_FOR_ABBREVIATION);
	}

	public String getLastModifiedDateToStr() {
		return DateUtils.dateToString(getLastModifiedDate());
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
	 * if www.test.com:0:0:0:0:0:ffff:3d87:a969 add ip: '0:0:0:0:0:ffff:3d87:a969'
	 *
	 * @return host ip list
	 */
	public List<String> getTargetHostIP() {
		List<String> targetIPList = new ArrayList<String>();
		String[] hostsList = StringUtils.split(StringUtils.trimToEmpty(targetHosts), ",");
		for (String hosts : hostsList) {
			String[] addresses = StringUtils.split(hosts, ":");
			if (addresses.length <= 2) {
				targetIPList.add(addresses[addresses.length - 1]);
			} else {
				targetIPList.add(hosts.substring(hosts.indexOf(":") + 1, hosts.length()));
			}
		}
		return targetIPList;
	}

	public void setTargetHosts(String theTarget) {
		this.targetHosts = theTarget;
	}

	public String getThreshold() {
		return threshold;
	}

	public Boolean isThresholdDuration() {
		return "D".equals(getThreshold());
	}

	public Boolean isThresholdRunCount() {
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

	public Integer getRampUpInitCount() {
		return rampUpInitCount;
	}

	public void setRampUpInitCount(Integer initProcesses) {
		this.rampUpInitCount = initProcesses;
	}

	public Integer getRampUpInitSleepTime() {
		return rampUpInitSleepTime;
	}


	public void setRampUpInitSleepTime(Integer initSleepTime) {
		this.rampUpInitSleepTime = initSleepTime;
	}

	public Integer getRampUpStep() {
		return rampUpStep;
	}


	public void setRampUpStep(Integer processIncrement) {
		this.rampUpStep = processIncrement;
	}

	public Integer getRampUpIncrementInterval() {
		return rampUpIncrementInterval;
	}


	public void setRampUpIncrementInterval(Integer processIncrementInterval) {
		this.rampUpIncrementInterval = processIncrementInterval;
	}

	public Integer getThreads() {
		return threads;
	}


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
		return DateUtils.ms2Time(this.duration);
	}


	/**
	 * Get Running time in HH:MM:SS style.
	 *
	 * @return formatted runtime string
	 */
	public String getRuntimeStr() {
		long ms = (this.finishTime == null || this.startTime == null) ? 0 : this.finishTime.getTime()
				- this.startTime.getTime();
		return DateUtils.ms2Time(ms);
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toStringExclude(this, "tags");
	}

	public String getProgressMessage() {
		return progressMessage;
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
		return lastProgressMessage;
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

	public Boolean getSafeDistribution() {
		return safeDistribution == null ? Boolean.FALSE : safeDistribution;
	}


	public void setSafeDistribution(Boolean safeDistribution) {
		this.safeDistribution = safeDistribution;
	}

	public String getRunningSample() {
		return runningSample;
	}


	public void setRunningSample(String runningSample) {
		this.runningSample = runningSample;
	}

	public String getAgentState() {
		return agentState;
	}

	public void setAgentState(String agentStatus) {
		this.agentState = agentStatus;
	}

	public String getMonitorState() {
		return monitorState;
	}

	public void setMonitorState(String monitorStatus) {
		this.monitorState = monitorStatus;
	}

	public Integer getSamplingInterval() {
		return samplingInterval;
	}


	public void setSamplingInterval(Integer samplingInterval) {
		this.samplingInterval = samplingInterval;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public void prepare(boolean isClone) {
		if (isClone) {
			this.setId(null);
			this.setTestComment("");
		}
		this.useRampUp = getSafe(this.useRampUp);
		this.safeDistribution = getSafe(this.safeDistribution);
	}
}
