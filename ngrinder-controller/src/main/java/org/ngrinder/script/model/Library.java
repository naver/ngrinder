package org.ngrinder.script.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.ngrinder.model.BaseEntity;

/**
 * Library entity
 * 
 * @author Liu Zhifei
 * @since 3.0
 */
@Entity
@Table(name = "LIBRARY")
public class Library extends BaseEntity<Library> {

	private static final long serialVersionUID = 4876896286845584908L;

	private String fileName;

	private long fileSize;

	private String fileType;

	private transient byte[] contentBytes;

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

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public byte[] getContentBytes() {
		return contentBytes;
	}

	public void setContentBytes(byte[] contentBytes) {
		this.contentBytes = contentBytes;
	}
}
