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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import net.grinder.common.GrinderProperties;

import org.ngrinder.common.util.DateUtil;
import org.ngrinder.model.BaseModel;
import org.springframework.data.jpa.domain.Specification;

/**
 * Peformance Test Entity
 * 
 */
@Entity
@Table(name = "PERF_TEST")
public class PerfTest extends BaseModel<PerfTest> {

	/**
	 * UUID
	 */
	private static final long serialVersionUID = 1369809450686098944L;

	@Column(name = "name")
	private String name;

	@Column(length = 2048)
	private String description;

	@Enumerated(EnumType.STRING)
	private Status status = Status.READY;

	private Date lastTestTime;

	/** The sample interval value, default to 1000ms */
	private int sampleInterval = 1000;

	/** ignoreSampleCount value, default to 0 */
	private int ignoreSampleCount;

	private int collectSampleCount;

	/** the target host to test */
	@Column(length = 256)
	private String targetHosts;

	/** The operation code. */
	private String operation;

	/** The result amount. */
	private int resultNum;

	/** The shared code */
	private boolean shared;

	/** The send mail code */
	private boolean sendMail;

	private String owner;

	/** The threshold code */
	private String threshold;

	// default script name to run test
	private String scriptName;

	private long duration;

	private Integer agentCount;
	@Transient
	private GrinderProperties grinderProperties;

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

	public String getName() {
		return name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTargetHosts() {
		return targetHosts;
	}

	public void setTargetHosts(String theTarget) {
		this.targetHosts = theTarget;
	}

	public Date getLastTestTime() {
		return lastTestTime;
	}

	public void setLastTestTime(Date lastTestTime) {
		this.lastTestTime = lastTestTime;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public int getResultNum() {
		return resultNum;
	}

	public void setResultNum(int resultNum) {
		this.resultNum = resultNum;
	}

	public boolean getShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
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

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwner() {
		return owner;
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

	public static Specification<PerfTest> statusSetEqual(final Status... statuses) {
		return new Specification<PerfTest>() {
			@Override
			
			public Predicate toPredicate(Root<PerfTest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.not(root.get("status").in((Object[]) statuses));
			}
		};

	}
}
