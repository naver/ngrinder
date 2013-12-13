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

import org.apache.commons.io.FilenameUtils;

/**
 * File type of FileEntity.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public enum FileType {
	/** Python/Jython. */
	PYTHON_SCRIPT("Jython Script", "py", FileCategory.SCRIPT, true, false),
	
	/** Groovy Script. */
	GROOVY_SCRIPT("Groovy Script", "groovy", FileCategory.SCRIPT, true, false),
	
	/** Groovy Maven project. */
	GROOVY_MAVEN_PROJECT("Groovy maven project", "pom", FileCategory.PROJECT, true, false),
	
	/** Xml. */
	XML("xml", "xml", FileCategory.DATA, true, true),
	/** Text. */
	TXT("txt", "txt", FileCategory.DATA, false, true),
	/** CSV. */
	CSV("csv", "csv", FileCategory.DATA, false, true),
	/** JSON. */
	JSON("json", "json", FileCategory.DATA, false, true),
	/** Properties. */
	PROPERTIES("properties", "properties", FileCategory.DATA, false, true),
	/** Classes. */
	CLASS("Java Class", "class", FileCategory.LIBRARY, true, false),
	/** Jar. */
	JAR("jar", "jar", FileCategory.LIBRARY, true, false),
	/** Dll for windows. */
	DLL("dll", "dll", FileCategory.LIBRARY, true, false),
	/** SO for linux. */
	SO("so", "so", FileCategory.LIBRARY, true, false),

	/** Unknown. */
	UNKNOWN("unknown", "", FileCategory.ETC, false, true),
	/** Dir. */
	DIR("dir", "", FileCategory.ETC, false, false);
	private String description;
	private final FileCategory fileCategory;
	private final String extension;
	private final boolean libDistributable;
	private final boolean resourceDistributable;

	/**
	 * Constructor.
	 * 
	 * @param description	description of this file type
	 * @param extension		file extension.
	 * @param fileCategory	category of FileType.
	 * @param libDistributable true if it's distributable as lib
	 * @param resourceDistributable true if it's distributable as resources
	 */
	FileType(String description, String extension, FileCategory fileCategory, boolean libDistributable,
					boolean resourceDistributable) {
		this.description = description;
		this.extension = extension;
		this.fileCategory = fileCategory;
		this.libDistributable = libDistributable;
		this.resourceDistributable = resourceDistributable;
	}

	/**
	 * Get file type by extension of given name.
	 * 
	 * @param name	name of file.
	 * @return FileType which matches to extension. UNKNOWN otherwise.
	 */
	public static FileType getFileTypeByName(String name) {
		return getFileTypeByExtension(FilenameUtils.getExtension(name));
	}

	/**
	 * Get file type by extension.
	 * 
	 * @param extension	extension
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
	 * 
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
	 * 
	 * @return true if distributable
	 */
	public boolean isLibDistributable() {
		return libDistributable;
	}

	/**
	 * Check if this file can be distributed in resource folder.
	 * 
	 * @return true if distributable
	 */
	public boolean isResourceDistributable() {
		return resourceDistributable;
	}

}
