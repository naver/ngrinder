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
package org.ngrinder.script.service;

import static org.ngrinder.common.util.Preconditions.checkNotEmpty;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.grinder.engine.agent.LocalScriptTestDriveService;
import net.grinder.util.thread.Condition;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.User;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Script Validation Service
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
public class ScriptValidationService {
	@Autowired
	private LocalScriptTestDriveService localScriptTestDriveService;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private Config config;

	/**
	 * Validate Script.
	 * 
	 * It's quite complex.. to validate script, we need write jar files and script. Furthermore, to make a small log..
	 * We have to copy optimized logback_worker.xml
	 * 
	 * Finally this method returns the path of validating result file.
	 * 
	 * @param user
	 *            user
	 * @param scriptEntry
	 *            scriptEntity.. at least path should be provided.
	 * @param useScriptInSVN
	 *            true if the script content in SVN is used. otherwise, false
	 * @return validation result.
	 */
	public String validateScript(User user, FileEntry scriptEntry, boolean useScriptInSVN) {
		try {
			checkNotNull(scriptEntry, "scriptEntity  should be not null");
			checkNotEmpty(scriptEntry.getPath(), "scriptEntity path should be provided");
			if (!useScriptInSVN) {
				checkNotEmpty(scriptEntry.getContent(), "scriptEntity content should be provided");
			}
			checkNotNull(user, "user should be provided");

			File scriptDirectory = config.getHome().getScriptDirectory(String.valueOf(user.getId()));

			// Get all files in the script path
			List<FileEntry> fileEntries = fileEntryService.getFileEntries(user,
					FilenameUtils.getPath(checkNotEmpty(scriptEntry.getPath())));

			scriptDirectory.mkdirs();

			// Distribute each files in that folder.
			for (FileEntry each : fileEntries) {
				// Directory is not subject to be distributed.
				if (each.getFileType() == FileType.DIR) {
					continue;
				}
				fileEntryService.writeContentTo(user, each.getPath(), scriptDirectory);
			}
			// Copy logback...
			File logbackXml = new ClassPathResource("/logback/logback-worker.xml").getFile();
			FileUtils.copyFile(logbackXml, new File(scriptDirectory, "logback-worker.xml"));
			File scriptFile = new File(scriptDirectory, FilenameUtils.getName(scriptEntry.getPath()));

			if (useScriptInSVN) {
				fileEntryService.writeContentTo(user, scriptEntry.getPath(), scriptDirectory);
			} else {
				FileUtils.writeStringToFile(scriptFile, scriptEntry.getContent(),
						StringUtils.defaultIfBlank(scriptEntry.getEncoding(), "UTF-8"));
			}

			File doValidate = localScriptTestDriveService.doValidate(scriptDirectory, scriptFile, new Condition(), "");
			return FileUtils.readFileToString(doValidate);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return StringUtils.EMPTY;
	}
}
