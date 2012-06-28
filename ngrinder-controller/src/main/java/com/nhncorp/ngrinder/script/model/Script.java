package com.nhncorp.ngrinder.script.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.nhncorp.ngrinder.core.model.BaseModel;

/**
 * Script entity
 * 
 * @author Liu Zhifei
 * @date 2012-6-13
 */
@Entity
@Table(name = "SCRIPT")
public class Script extends BaseModel {

	private static final long serialVersionUID = -2422243194192027508L;

	private String fileName;

	private long fileSize;

	private String testURL;

	private String language;

	private transient byte[] contentBytes;

	private transient String content;

	private transient String historyContent;

	private transient String cacheContent;

	private transient List<String> historyFileNames;

	// private ScriptType type;

	private boolean share = false;

	private String description;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "SCRIPT_TAG", joinColumns = @JoinColumn(name = "SCRIPT_ID"), inverseJoinColumns = @JoinColumn(name = "TAG_ID"))
	private List<Tag> tags = new ArrayList<Tag>();

	private String tagsString;

	private Date lastTestDate;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.getCreateDate() == null) ? 0 : this.getCreateDate().hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((testURL == null) ? 0 : testURL.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Script other = (Script) obj;
		if (this.getCreateDate() == null) {
			if (other.getCreateDate() != null) {
				return false;
			}
		} else if (!this.getCreateDate().equals(other.getCreateDate())) {
			return false;
		}
		if (fileName == null) {
			if (other.fileName != null) {
				return false;
			}
		} else if (!fileName.equals(other.fileName)) {
			return false;
		}
		if (testURL == null) {
			if (other.testURL != null) {
				return false;
			}
		} else if (!testURL.equals(other.testURL)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Script [fileName=" + fileName + ", fileSize=" + fileSize + ", testURL=" + testURL + ", share=" + share
				+ ", description=" + description + ", tags=" + tags + "]";
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getTestURL() {
		return testURL;
	}

	public void setTestURL(String testURL) {
		this.testURL = testURL;
	}

	public byte[] getContentBytes() {
		return contentBytes;
	}

	public void setContentBytes(byte[] contentBytes) {
		this.fileSize = contentBytes.length;
		this.contentBytes = contentBytes;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.fileSize = content.length();
		this.content = content;
	}

	public String getHistoryContent() {
		return historyContent;
	}

	public void setHistoryContent(String historyContent) {
		this.historyContent = historyContent;
	}

	public String getCacheContent() {
		return cacheContent;
	}

	public void setCacheContent(String cacheContent) {
		this.cacheContent = cacheContent;
	}

	public List<String> getHistoryFileNames() {
		return historyFileNames;
	}

	public void setHistoryFileNames(List<String> historyFileNames) {
		this.historyFileNames = historyFileNames;
	}

	public boolean isShare() {
		return share;
	}

	public void setShare(boolean share) {
		this.share = share;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public void addTag(Tag tag) {
		this.tags.add(tag);
	}

	public Date getLastTestDate() {
		return lastTestDate;
	}

	public void setLastTestDate(Date lastTestDate) {
		this.lastTestDate = lastTestDate;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getTagsString() {
		return tagsString;
	}

	public void setTagsString(String tagsString) {
		this.tagsString = tagsString;
	}

}
