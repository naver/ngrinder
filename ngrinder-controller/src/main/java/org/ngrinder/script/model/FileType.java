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

import org.apache.commons.io.FilenameUtils;

/**
 * File type of FileEntity.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public enum FileType {
	/** Python. */
	PYTHON_SCRIPT("Python Script", "py", FileCategory.SCRIPT, true, false),
	/** Java Script. */
	JAVA_SCRIPT("JavaScript", "js", FileCategory.SCRIPT, true, false),
	/** Xml. */
	XML("xml", "xml", FileCategory.DATA, false, true),
	/** Text. */
	TXT("txt", "txt", FileCategory.DATA, false, true),
	/** CSV. */
	CSV("csv", "csv", FileCategory.DATA, false, true),
	/** JSON. */
	JSON("json", "json", FileCategory.DATA, false, true),
	/** Properties. */
	PROPERTIES("properties", "properties", FileCategory.DATA, false, true),
	/** Classes */
	CLASS("Java Class", "class", FileCategory.LIBRARY, true, false),
	/** Jar. */
	JAR("jar", "jar", FileCategory.LIBRARY, true, false),
	/** Unknown. */
	UNKNOWN("unknown", "", FileCategory.ETC, false, false),
	/** Dir. */
	DIR("dir", "", FileCategory.ETC, false, false);
	private String description;
	private final FileCategory fileCategory;
	private final String extension;
	private final boolean libDistribtable;
	private final boolean resourceDistributable;

	/**
	 * Constructor.
	 * @param description description of this file type
	 * @param extension extendsion used.
	 * @param fileCategory category of FileType.
	 */
	FileType(String description, String extension, FileCategory fileCategory, boolean libDistribtable, boolean resourceDistributable) {
		this.description = description;
		this.extension = extension;
		this.fileCategory = fileCategory;
		this.libDistribtable = libDistribtable;
		this.resourceDistributable = resourceDistributable;
	}

	/**
	 * Get file type by extension of given name.
	 * @param name name of file.
	 * @return FileType which matches to extension. UNKNOWN otherwise.
	 */
	public static FileType getFileTypeByName(String name) {
		return getFileTypeByExtension(FilenameUtils.getExtension(name));
	}
	
	/**
	 * Get file type by extension.
	 * @param extension extension
	 * @return FileType which matches to extension. UNKNOWN otherwise.
	 */
	public static FileType getFileTypeByExtension(String extension) {
		for (FileType each : values()) {
			if (each.extension.equals(extension)) {
				return each;
			}
		}
		return UNKNOWN;
	}

	/**
	 * Get description of file type.
	 * @return file type description. 
	 */
	public String toString() {
		return description;
	}

	public FileCategory getFileCategory() {
		return fileCategory;
	}

	public boolean isEditable() {
		return fileCategory.isEditable();
	}

	/**
	 * Check if this file can be distributed in lib folder.
	 * @return true if distributable
	 */
	public boolean isLibDistribtable() {
		return libDistribtable;
	}

	/**
	 * Check if this file can be distributed in resource folder.
	 * @return true if distributable
	 */
	public boolean isResourceDistributable() {
		return resourceDistributable;
	}

}
