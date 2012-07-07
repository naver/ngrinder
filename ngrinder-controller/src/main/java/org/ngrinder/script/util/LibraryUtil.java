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
package org.ngrinder.script.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.infra.config.Config;
import org.ngrinder.script.model.Library;
import org.ngrinder.user.service.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Library util
 * 
 * @author Tobi
 * @since 3.0
 */
@Component
public class LibraryUtil implements NGrinderConstants {
	private final Logger LOG = LoggerFactory.getLogger(LibraryUtil.class);

	@Autowired
	private UserContext userContext;

	@Autowired
	private Config config;

	public void createLibraryPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(config.getHome().getProjectDirectory().getAbsolutePath());
		sb.append(File.separator);
		sb.append(PREFIX_USER);
		sb.append(userContext.getCurrentUser().getId());
		sb.append(File.separator);
		sb.append(PATH_LIB);
		sb.append(File.separator);

		String libPath = sb.toString();
		File dir = new File(libPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	public String getLibraryPath() {
		StringBuilder sb = new StringBuilder();
		sb.append(config.getHome().getProjectDirectory().getAbsolutePath());
		sb.append(File.separator);
		sb.append(PREFIX_USER);
		sb.append(userContext.getCurrentUser().getId());
		sb.append(File.separator);
		sb.append(PATH_LIB);
		sb.append(File.separator);

		return sb.toString();
	}

	public String getLibFilePath(String libName) {
		return getLibraryPath() + libName;
	}

	public List<Library> getLibrary() {
		List<Library> librarys = new ArrayList<Library>();

		String libPath = getLibraryPath();
		File libDir = new File(libPath);
		File[] libFiles = libDir.listFiles();
		if (null != libFiles) {
			for (File libFile : libFiles) {
				Library library = new Library();
				library.setFileName(libFile.getName());
				library.setFileSize(libFile.length());
				library.setFileType(libFile.getName().substring(libFile.getName().lastIndexOf('.') + 1));
				librarys.add(library);
			}
		}
		return librarys;
	}

	public void saveLibraryFile(Library library) {
		String libPath = getLibraryPath();
		String libFilePath = libPath + library.getFileName();
		try {
			FileUtils.writeByteArrayToFile(new File(libFilePath), library.getContentBytes());
		} catch (IOException e) {
			LOG.error("Write script file failed.", e);
		}
	}

	public void deleteLibraryFile(String libraryName) {
		String libPath = getLibraryPath();
		String libFilePath = libPath + libraryName;
		FileUtils.deleteQuietly(new File(libFilePath));
	}
}
