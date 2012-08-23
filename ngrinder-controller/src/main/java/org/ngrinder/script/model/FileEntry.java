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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.ngrinder.model.BaseModel;

/**
 * File entity which will be stored in SVN.
 * 
 * 
 * @author Liu Zhifei
 * @author JunHo Yoon
 * @since 3.0
 */
public class FileEntry extends BaseModel<FileEntry> {

	private static final long serialVersionUID = -2422243194192027508L;

	private long fileSize;

	/**
	 * Test url. This File entity works on.. This field is only for the HTML form.
	 */
	private String testURL;

	private String content;

	/**
	 * File properties
	 */
	private Map<String, String> properties = new HashMap<String, String>();
	/**
	 * Revisions on this entity. This fields are sometimes empty depending on the {@link FileEntryRepository}
	 */
	private List<Long> revisions;

	/**
	 * This is mapped to commit comment.
	 */
	private String description;

	private String encoding;

	private byte[] contentBytes;

	private String path;

	private FileType fileType;

	private long revision;

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

	/**
	 * set content bytes.
	 * 
	 * @param contentBytes
	 *            contentByte.
	 */
	public void setContentBytes(byte[] contentBytes) {
		this.fileSize = contentBytes.length;
		this.contentBytes = contentBytes;
	}

	public String getContent() {
		return content;
	}

	/**
	 * Set content in string form.
	 * 
	 * @param content
	 *            content string
	 */
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
		if (fileType == null) {
			fileType = FileType.getFileTypeByExtension(FilenameUtils.getExtension(getPath()));
		}
		return fileType;
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

	/**
	 * Get current revision.
	 * 
	 * @return the revision
	 */
	public long getRevision() {
		return revision;
	}

	/**
	 * Set current revision.
	 * 
	 * @param revision
	 *            the revision to set
	 */
	public void setRevision(long revision) {
		this.revision = revision;
	}

	public Map<String, String> getProperties() {
		return this.properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

}
