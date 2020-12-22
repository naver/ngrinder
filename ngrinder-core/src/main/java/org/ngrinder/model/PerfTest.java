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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.grinder.common.GrinderProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import static java.util.Date.from;
import static org.apache.commons.lang.ObjectUtils.defaultIfNull;
import static org.ngrinder.common.util.AccessUtils.getSafe;
import static org.ngrinder.common.util.DateUtils.dateToString;
import static org.ngrinder.common.util.DateUtils.ms2Time;
import static org.ngrinder.common.util.TypeConvertUtils.cast;

/**
 * Performance Test Entity.
 */

@SuppressWarnings({"JpaDataSourceORMInspection", "UnusedDeclaration", "JpaAttributeTypeInspection", "DanglingJavadoc"})
@Getter
@Setter
@Entity
@Table(name = "PERF_TEST")
public class PerfTest extends BaseModel<PerfTest> {

	private static final int MARGIN_FOR_ABBREVIATION = 8;

	private static final int MAX_LONG_STRING_SIZE = 2048;

	private static final long serialVersionUID = 1369809450686098944L;

	private static final int MAX_STRING_SIZE = 2048;

	private static final String DEFAULT_SCM = "svn";

	public PerfTest() {

	}

	/**
	 * Constructor.
	 *
	 * @param createdUser crested user.
	 */
	public PerfTest(User createdUser) {
		this.setCreatedBy(createdUser);
		this.setLastModifiedBy(createdUser);
	}

	@Cloneable
	@Column(name = "name")
	private String testName;

	@Cloneable
	@Column(name = "tag_string")
	private String tagString;

	@Cloneable
	@Column(length = MAX_LONG_STRING_SIZE)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private Status status;

	@Cloneable
	/** ignoreSampleCount value, default to 0. */
	@Column(name = "ignore_sample_count")
	private Integer ignoreSampleCount;

	/** the scheduled time of this test. */
	@Column(name = "scheduled_time")
	private Instant scheduledTime;

	/** the start time of this test. */
	@Column(name = "start_time")
	private Instant startTime;

	/** the finish time of this test. */
	@Column(name = "finish_time")
	private Instant finishTime;

	/**
	 * the target host to test.
	 */
	@Cloneable
	@Column(name = "target_hosts", length = 65535)
	private String targetHosts;

	/**
	 * The send mail code.
	 */
	@Cloneable
	@Column(name = "send_mail", columnDefinition = "char(1)")
	@Type(type = "true_false")
	private Boolean sendMail;

	/**
	 * Use rampUp or not.
	 */
	@Cloneable
	@Column(name = "use_rampup", columnDefinition = "char(1)")
	@Type(type = "true_false")
	private Boolean useRampUp;

	/**
	 * Use rampUp or not.
	 */
	@Cloneable
	@Column(name = "ramp_up_type")
	@Enumerated(EnumType.STRING)
	private RampUp rampUpType;

	/**
	 * The threshold code, R for run count; D for duration.
	 */
	@Cloneable
	@Column(name = "threshold")
	private String threshold;

	@Cloneable
	@Column(name = "scm")
	private String scm;

	@Cloneable
	@Column(name = "script_name")
	private String scriptName;

	@Cloneable
	@Column(name = "duration")
	private Long duration;

	@Cloneable
	@Column(name = "run_count")
	private Integer runCount;

	@Cloneable
	@Column(name = "agent_count")
	private Integer agentCount;

	@Cloneable
	@Column(name = "vuser_per_agent")
	private Integer vuserPerAgent;

	@Cloneable
	@Column(name = "processes")
	private Integer processes;

	@Cloneable
	@Column(name = "ramp_up_init_count")
	private Integer rampUpInitCount;

	@Cloneable
	@Column(name = "ramp_up_init_sleep_time")
	private Integer rampUpInitSleepTime;

	@Cloneable
	@Column(name = "ramp_up_step")
	private Integer rampUpStep;

	@Cloneable
	@Column(name = "ramp_up_increment_interval")
	private Integer rampUpIncrementInterval;

	@Cloneable
	@Column(name = "threads")
	private Integer threads;

	// followings are test result members
	@Column(name = "tests")
	private Long tests;

	@Column(name = "errors")
	private Long errors;

	@Column(name = "mean_test_time")
	private Double meanTestTime;

	@Column(name = "test_time_standard_deviation")
	private Double testTimeStandardDeviation;

	@Column(name = "tps")
	private Double tps;

	@Column(name = "peak_tps")
	private Double peakTps;

	/**
	 * Console port for this test. This is the identifier for console
	 */
	@JsonIgnore
	@Column(name = "port")
	private Integer port;

	@Column(name = "test_error_cause")
	@Enumerated(EnumType.STRING)
	private Status testErrorCause;

	@JsonIgnore
	@Column(name = "distribution_path")
	/** The path used for file distribution */
	private String distributionPath;

	@Column(name = "progress_message", length = MAX_STRING_SIZE)
	private String progressMessage;

	@Column(name = "last_progress_message", length = MAX_STRING_SIZE)
	private String lastProgressMessage;

	@Column(name = "test_comment", length = MAX_STRING_SIZE)
	private String testComment;

	@Column(name = "script_revision")
	private String scriptRevision;

	@Column(name = "stop_request")
	@Type(type = "true_false")
	@Getter(AccessLevel.NONE)
	private Boolean stopRequest;

	@Cloneable
	@Column(name = "region")
	private String region;

	@Column(name = "safe_distribution", columnDefinition = "char(1)")
	@Cloneable
	@Type(type = "true_false")
	private Boolean safeDistribution;

	@Column(name = "ignore_too_many_error", columnDefinition = "char(1)")
	@Cloneable
	@Type(type = "true_false")
	private Boolean ignoreTooManyError;

	@JsonIgnore
	@Transient
	private GrinderProperties grinderProperties;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	@JoinTable(name = "PERF_TEST_TAG", /** join column */
			joinColumns = @JoinColumn(name = "perf_test_id"), /** inverse join column */
			inverseJoinColumns = @JoinColumn(name = "tag_id"))
	@SortNatural
	private SortedSet<Tag> tags;

	@Cloneable
	@Column(name = "sampling_interval")
	private Integer samplingInterval;

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
		this.stopRequest = getSafe(this.stopRequest, false);
		this.duration = getSafe(this.duration, 60000L);
		this.samplingInterval = getSafe(this.samplingInterval, 2);
		this.scriptRevision = getSafe(this.scriptRevision, "-1");
		this.param = getSafe(this.param, "");
		this.scm = getSafe(this.scm, DEFAULT_SCM);
		this.region = getSafe(this.region, "NONE");
		this.targetHosts = getSafe(this.targetHosts, "");
		this.description = getSafe(this.description, "");
		this.tagString = getSafe(this.tagString, "");
		this.vuserPerAgent = getSafe(this.vuserPerAgent, 1);
		this.safeDistribution = getSafe(this.safeDistribution, false);
		this.ignoreTooManyError = getSafe(this.ignoreTooManyError, false);
		this.useRampUp = getSafe(this.useRampUp, false);
		this.rampUpInitCount = getSafe(this.rampUpInitCount, 0);
		this.rampUpStep = getSafe(this.rampUpStep, 1);
		this.rampUpInitSleepTime = getSafe(this.rampUpInitSleepTime, 0);
		this.rampUpIncrementInterval = getSafe(this.rampUpIncrementInterval, 1000);
		this.rampUpType = getSafe(this.rampUpType, RampUp.PROCESS);
	}

	@JsonIgnore
	public String getTestIdentifier() {
		return "perftest_" + getId() + "_" + getLastModifiedBy().getUserId();
	}

	/**
	 * Get total required run count. This is calculated by multiplying agent count, threads,
	 * processes, run count.
	 *
	 * @return run count
	 */
	@JsonIgnore
	public long getTotalRunCount() {
		return getAgentCount() * getThreads() * getProcesses() * (long) getRunCount();
	}

	public String getDescription() {
		return StringUtils.abbreviate(description, MAX_LONG_STRING_SIZE - MARGIN_FOR_ABBREVIATION);
	}

	@JsonIgnore
	public String getLastModifiedAtToStr() {
		return dateToString(from(getLastModifiedAt()));
	}

	/**
	 * Get ip address of target hosts. if target hosts 'a.com:1.1.1.1' add ip: '1.1.1.1' if target
	 * hosts ':1.1.1.1' add ip: '1.1.1.1' if target hosts '1.1.1.1' add ip: '1.1.1.1'
	 * if www.test.com:0:0:0:0:0:ffff:3d87:a969 add ip: '0:0:0:0:0:ffff:3d87:a969'
	 *
	 * @return host ip list
	 */
	@JsonIgnore
	public List<String> getTargetHostIP() {
		List<String> targetIPList = new ArrayList<>();
		String[] hostsList = StringUtils.split(StringUtils.trimToEmpty(targetHosts), ",");
		for (String hosts : hostsList) {
			String[] addresses = StringUtils.split(hosts, ":");
			if (addresses.length <= 2) {
				targetIPList.add(addresses[addresses.length - 1]);
			} else {
				targetIPList.add(hosts.substring(hosts.indexOf(":") + 1));
			}
		}
		return targetIPList;
	}

	@JsonIgnore
	public Boolean isThresholdDuration() {
		return "D".equals(getThreshold());
	}

	@JsonIgnore
	public Boolean isThresholdRunCount() {
		return "R".equals(getThreshold());
	}

	public boolean isStopRequest() {
		return getSafe(stopRequest, false);
	}

	/**
	 * Get Running time in HH:MM:SS style.
	 *
	 * @return formatted runtime string
	 */
	@JsonProperty("runtime")
	public String getRuntimeStr() {
		long runtimeSecond = (this.finishTime == null || this.startTime == null) ? 0 : this.finishTime.getEpochSecond() - this.startTime.getEpochSecond();
		return ms2Time(runtimeSecond * 1000);
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toStringExclude(this, "tags");
	}

	public void setProgressMessage(String progressMessage) {
		this.progressMessage = StringUtils.defaultIfEmpty(StringUtils.right(progressMessage, MAX_STRING_SIZE), "");
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
			if (!StringUtils.isEmpty(this.lastProgressMessage)) {
				setProgressMessage(getProgressMessage() + this.lastProgressMessage + "\n");
			}
		}
		this.lastProgressMessage = lastProgressMessage;
	}

	public void setTestComment(String testComment) {
		this.testComment = StringUtils.trimToEmpty(StringUtils.right(testComment, MAX_STRING_SIZE));
	}

	/**
	 * Clear all messages.
	 */
	public void clearMessages() {
		clearLastProgressMessage();
		setProgressMessage("");
	}

	public Boolean getSafeDistribution() {
		return cast(defaultIfNull(safeDistribution, Boolean.FALSE));
	}

	public Boolean getIgnoreTooManyError() {
		return cast(defaultIfNull(ignoreTooManyError, Boolean.FALSE));
	}

	public boolean isGitHubScm() {
		return scm != null && !scm.equals(DEFAULT_SCM);
	}

	public void prepare(boolean isClone) {
		if (isClone) {
			this.setId(null);
			this.setTestComment("");
		}
		this.useRampUp = getSafe(this.useRampUp);
		this.safeDistribution = getSafe(this.safeDistribution);
		this.ignoreTooManyError = getSafe(this.ignoreTooManyError);
	}
}
