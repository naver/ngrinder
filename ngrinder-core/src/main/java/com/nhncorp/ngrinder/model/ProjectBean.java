package com.nhncorp.ngrinder.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.nhncorp.ngrinder.util.DateUtil;

import net.grinder.common.GrinderProperties;

/**
 * The Class Project.
 * 
 */
public class ProjectBean {

	private Integer id;

	private String name;

	private String description;

	private Date lastModifiedDate;

	private Date lastTestTime;

	/** The sample interval value, default to 1000ms */
	private int sampleInterval = 1000;

	/** ignoreSampleCount value, default to 0 */
	private int ignoreSampleCount;

	private int collectSampleCount;

	/** the target host to test */
	private String targetHosts;

	/** The shared user list. */
	private List<NGrinderUserBean> sharedUserList;

	/** The shared user id list. */
	private List<String> sharedUserId;

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

	/** used by /etc/hosts */
	private String hostsIp;

	/** used by /etc/hosts */
	private String hostsDomain;

	// default script name to run test
	private String scriptName;

	// default script id to run test
	// private Integer scriptId;

	private long duration;

	// private String userId;

	// private int vuser;

	private GrinderProperties grinderProperties;

	// public int getVuser() {
	// return vuser;
	// }
	//
	// public void setVuser(int vuser) {
	// this.vuser = vuser;
	// }

	// public String getUserId() {
	// return userId;
	// }
	//
	// public void setUserId(String userId) {
	// this.userId = userId;
	// }

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

	public Integer getId() {
		return id;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public String getLastModifiedDateToStr() {
		return DateUtil.formatDate(lastModifiedDate, "yyyy-MM-dd  HH:mm:ss");
	}

	public String getName() {
		return name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
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

	public List<NGrinderUserBean> getSharedUserList() {
		return sharedUserList;
	}

	public void setSharedUserList(List<NGrinderUserBean> sharedUserList) {
		this.sharedUserList = sharedUserList;
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

	public List<String> getSharedUserId() {
		return sharedUserId == null ? new ArrayList<String>() : sharedUserId;
	}

	public void setSharedUserId(List<String> sharedUserId) {
		this.sharedUserId = sharedUserId;
	}

	public String getHostsIp() {
		return hostsIp;
	}

	public void setHostsIp(String hostsIp) {
		this.hostsIp = hostsIp;
	}

	public String getHostsDomain() {
		return hostsDomain;
	}

	public void setHostsDomain(String hostsDomain) {
		this.hostsDomain = hostsDomain;
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
		return "ProjectBean [name=" + name + "]";
	}

}
