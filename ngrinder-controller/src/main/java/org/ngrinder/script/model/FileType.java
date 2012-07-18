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

public enum FileType {
	/** Python */
	PYTHON_SCRIPT("Python Script", "py", FileCategory.SCRIPT),
	/** Java Script */
	JAVA_SCRIPT("JavaScript", "js", FileCategory.SCRIPT),
	/** Xml */
	XML("xml", "xml", FileCategory.DATA),
	/** Text */
	TXT("txt", "txt", FileCategory.DATA),
	/** CSV */
	CSV("csv", "csv", FileCategory.DATA),
	/** Properties */
	PROPERTIES("properties", "properties", FileCategory.DATA),
	/** Jar */
	JAR("jar", "jar", FileCategory.LIBRARY),
	/** Unknown */
	UNKNOWN("unknown", "", FileCategory.ETC),
	/** Dir */
	DIR("dir", "", FileCategory.ETC);
	private String description;
	private final FileCategory fileCategory;
	private final String extension;

	FileType(String description, String extension, FileCategory fileCategory) {
		this.description = description;
		this.extension = extension;
		this.fileCategory = fileCategory;
	}

	public static FileType getFileType(String extension) {
		for (FileType each : values()) {
			if (each.extension.equals(extension)) {
				return each;
			}
		}
		return UNKNOWN;
	}

	public String toString() {
		return description;
	}

	public FileCategory getFileCategory() {
		return fileCategory;
	}

}
