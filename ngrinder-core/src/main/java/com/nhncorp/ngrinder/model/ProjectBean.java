package com.nhncorp.ngrinder.model;

import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.grinder.common.GrinderProperties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.nhncorp.ngrinder.core.model.BaseModel;
import com.nhncorp.ngrinder.util.DateUtil;

/**
 * The Class Project.
 * 
 */
@Entity
@Table(name = "project")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "project_id")) })
public class ProjectBean extends BaseModel {
	/**
	 * UUID
	 */
	private static final long serialVersionUID = 1369809450686098944L;

	@Column(name = "project_name")
	private String name;

	@Column(length = 2048)
	private String description;

	private Date lastTestTime;

	/** The sample interval value, default to 1000ms */
	private int sampleInterval = 1000;

	/** ignoreSampleCount value, default to 0 */
	private int ignoreSampleCount;

	private int collectSampleCount;

	/** the target host to test */
	@Column(length = 256)
	private String targetHosts;

	/** The shared user list. */
	/** private List<NGrinderUserBean> sharedUserList; */

	/** The shared user id list. */
	/**
	 * private List<String> sharedUserId;
	 */

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

	/**
	 * used by /etc/hosts
	 * 
	 * Comma Separated host mapping like
	 * www.helloworld.com:10.98.223.22,www.helloworld2.com:10.22.22.33
	 */
	private String hostsMapping;

	// default script name to run test
	private String scriptName;

	private long duration;

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

	// public Integer getScriptId() {
	// return scriptId;
	// }
	//
	// public void setScriptId(Integer scriptId) {
	// this.scriptId = scriptId;
	// }

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

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public String getHostsMapping() {
		return hostsMapping;
	}

	public void setHostsMapping(String hostsMapping) {
		this.hostsMapping = hostsMapping;
	}

}
