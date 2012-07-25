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
package org.ngrinder.script.model;

import static org.ngrinder.common.util.Preconditions.checkNotEmpty;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.io.FilenameUtils;
import org.ngrinder.model.BaseModel;

/**
 * Script entity
 * 
 * @author Liu Zhifei
 * @author JunHo Yoon
 * @since 3.0
 */
@Entity
@Table(name = "SCRIPT")
public class FileEntry extends BaseModel<FileEntry> {

	private static final long serialVersionUID = -2422243194192027508L;

	private long fileSize;

	private String testURL;

	private transient String content;

	private transient List<Long> revisions;

	private String description;

	private String encoding;

	private byte[] contentBytes;

	private String path;

	private FileType fileType;

	public String getPath() {
		return path;
	}

	public String getFileName() {
		return FilenameUtils.getName(checkNotEmpty(getPath()));
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isEditable() {
		return fileType.getFileCategory().isEditable();
	}

	public FileType getFileType() {
		return (fileType == null) ? FileType.getFileType(FilenameUtils.getExtension(getPath())) : fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public List<Long> getRevisions() {
		return revisions;
	}

	public void setRevisions(List<Long> revisions) {
		this.revisions = revisions;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

}
