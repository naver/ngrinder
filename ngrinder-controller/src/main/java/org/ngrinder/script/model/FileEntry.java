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
package org.ngrinder.script.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.ngrinder.common.util.PathUtils;
import org.ngrinder.model.BaseModel;
import org.ngrinder.model.IFileEntry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.Preconditions.checkNotEmpty;

/**
 * File entity which will be stored in SVN.
 *
 * @author Liu Zhifei
 * @author JunHo Yoon
 * @since 3.0
 */
public class FileEntry extends BaseModel<FileEntry> implements IFileEntry {

	private static final long serialVersionUID = -2422243194192027508L;

	private long fileSize;

	private String content;

	/**
	 * File properties.
	 */
	private Map<String, String> properties = new HashMap<>();

	/**
	 * This is mapped to commit comment.
	 */
	private String description;

	private String encoding;

	@JsonIgnore
	private byte[] contentBytes;

	private String path;

	private FileType fileType;

	private long revision;

	private long lastRevision;

	/**
	 * Get path of entry.
	 *
	 * @return path
	 */
	@JsonSerialize(using = UnixPathSerializer.class)
	public String getPath() {
		return path;
	}

	@JsonSerialize(using = UnixPathSerializer.class)
	public String getPathInShort() {
		return PathUtils.getShortPath(path);
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


	public byte[] getContentBytes() {
		return contentBytes;
	}

	/**
	 * set content bytes.
	 *
	 * @param contentBytes contentByte.
	 */
	public void setContentBytes(byte[] contentBytes) {
		this.fileSize = contentBytes.length;
		this.contentBytes = contentBytes;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ngrinder.script.model.IFileEntry#getContent()
	 */
	@Override
	public String getContent() {
		return content;
	}

	/**
	 * Set content in string form.
	 *
	 * @param content content string
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

	@SuppressWarnings("UnusedDeclaration")
	public boolean isEditable() {
		return getFileType().getFileCategory().isEditable();
	}

	/**
	 * Get file type. If fileType is set, it returns the set fileType.
	 * Otherwise, it tries to detect the file type by the extension.
	 *
	 * @return file type.
	 */
	public FileType getFileType() {
		if (fileType == null) {
			fileType = FileType.getFileTypeByExtension(FilenameUtils.getExtension(getPath()));
		}
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
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
	 * @param revision the revision to set
	 */
	public void setRevision(long revision) {
		this.revision = revision;
	}

	/**
	 * Get properties.
	 *
	 * @return properties, empty map if null.
	 */
	public Map<String, String> getProperties() {
		if (this.properties == null) {
			return newHashMap();
		}
		return this.properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public long getLastRevision() {
		return lastRevision;
	}

	public void setLastRevision(long lastRevision) {
		this.lastRevision = lastRevision;
	}

	public int getValidated() {
		String validateValue = this.getProperties().getOrDefault("validated", "0");
		if (NumberUtils.isNumber(validateValue)) {
			return NumberUtils.createInteger(validateValue);
		} else {
			return 0;
		}
	}

	private static class UnixPathSerializer extends StdSerializer<String> {
		@SuppressWarnings("unused")
		UnixPathSerializer() {
			this(null);
		}

		UnixPathSerializer(Class<String> t) {
			super(t);
		}

		@Override
		public void serialize(String path, JsonGenerator generator, SerializerProvider provider) throws IOException {
			generator.writeObject(FilenameUtils.separatorsToUnix(path));
		}
	}
}
