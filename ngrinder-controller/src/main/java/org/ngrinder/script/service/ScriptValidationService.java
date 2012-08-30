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
import java.util.ArrayList;
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
import org.python.core.PySyntaxError;
import org.python.core.PyTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger LOG = LoggerFactory
			.getLogger(ScriptValidationService.class);
	@Autowired
	private LocalScriptTestDriveService localScriptTestDriveService;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private Config config;

	/**
	 * Validate Script.
	 * 
	 * It's quite complex.. to validate script, we need write jar files and
	 * script. Furthermore, to make a small log.. We have to copy optimized
	 * logback_worker.xml
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
	public String validateScript(User user, FileEntry scriptEntry,
			boolean useScriptInSVN, String hostString) {
		try {
			checkNotNull(scriptEntry, "scriptEntity should be not null");
			checkNotEmpty(scriptEntry.getPath(),
					"scriptEntity path should be provided");
			if (!useScriptInSVN) {
				checkNotEmpty(scriptEntry.getContent(),
						"scriptEntity content should be provided");
			}
			checkNotNull(user, "user should be provided");
			String result = checkSyntaxErrors(scriptEntry.getContent());
			if (result != null) {
				return result;
			}
			File scriptDirectory = config.getHome().getScriptDirectory(
					String.valueOf(user.getId()));
			File file = new File(scriptDirectory, "validation-0.log");
			file.delete();
			// Get all lib and resources in the script path
			List<FileEntry> fileEntries = fileEntryService
					.getLibAndResourcesEntries(user,
							checkNotEmpty(scriptEntry.getPath()), null);

			scriptDirectory.mkdirs();

			// Distribute each files in that folder.
			for (FileEntry each : fileEntries) {
				// Directory is not subject to be distributed.
				if (each.getFileType() == FileType.DIR) {
					continue;
				}
				fileEntryService.writeContentTo(user, each.getPath(),
						scriptDirectory);
			}
			// Copy logback...
			File logbackXml = new ClassPathResource(
					"/logback/logback-worker.xml").getFile();
			FileUtils.copyFile(logbackXml, new File(scriptDirectory,
					"logback-worker.xml"));
			File scriptFile = new File(scriptDirectory,
					FilenameUtils.getName(scriptEntry.getPath()));

			// Write the policy file in the folder: scriptDirectory
			// Do not limit IP access but file access. (Home folder..)
			//DefaultSecurityPolicyGenerator securityPolicyGenerator = Generators.newDefaultSecurityPolicyGenerator();
			List<String> ipList = this.getHostIP(hostString);
			// for (String ip : ipList) {
			// securityPolicyGenerator.allowNetworkAccess(ip);
			// }
			// // add all file access in current folder
//			securityPolicyGenerator.allowFileAccess("-", ActionMode.FILE_ALL_ACTION);
//			securityPolicyGenerator.allowFileAccess(".", ActionMode.FILE_READ_ACTION);
//			securityPolicyGenerator.allowFileAccess(this.getLibPath() + "*", ActionMode.FILE_READ_ACTION);

			// File scriptPolicy = new File(scriptDirectory, "script.policy");
			// try {
			// // securityPolicyGenerator.write(scriptPolicy);
			// } catch (IOException e) {
			// LOG.error("Write script's policy file failed.", e);
			// }
			String jvmArguments = "";
			// if (scriptPolicy.exists()) {
			// String policyFilePath = scriptPolicy.getAbsolutePath();
			// jvmArguments = "-Djava.security.policy=" + policyFilePath +
			// " -Djava.security.manager";
			// }

			if (useScriptInSVN) {
				fileEntryService.writeContentTo(user, scriptEntry.getPath(),
						scriptDirectory);
			} else {
				FileUtils.writeStringToFile(scriptFile, scriptEntry
						.getContent(), StringUtils.defaultIfBlank(
						scriptEntry.getEncoding(), "UTF-8"));
			}
			File doValidate = localScriptTestDriveService.doValidate(scriptDirectory, scriptFile, new Condition(),
					jvmArguments);
			return FileUtils.readFileToString(doValidate);
		} catch (IOException e) {
			LOG.error("Error while distributing files on {} for {}", user,
					scriptEntry.getPath());
			LOG.error("Error details ", e);
		}
		return StringUtils.EMPTY;
	}
	
	private String getLibPath() {
		String path = this.getClass().getResource("/").getPath();
		path = path.substring(1, path.length() - 1);
		String str = "classes";
		int i = path.indexOf(str);
		if (i > 0) {
			path = path.substring(0, i);
			path += "lib/";
		}
		return path;
	}

	private List<String> getHostIP(String hostString) {
		List<String> ipList = new ArrayList<String>();
		String[] hostsList = StringUtils.split(hostString, ",");
		for (String hosts : hostsList) {
			String[] addresses = StringUtils.split(hosts, ":");
			if (addresses.length > 0) {
				ipList.add(addresses[addresses.length - 1]);
			} else {
				ipList.add(hosts);
			}
		}
		return ipList;
	}

	/**
	 * Run jython parser to find out the syntax error..
	 * 
	 * @param script
	 * @return script syntax error message. null otherwise.
	 */
	public String checkSyntaxErrors(String script) {
		try {
			org.python.core.parser.parse(script, "exec");
		} catch (PySyntaxError e) {
			try {
				PyTuple pyTuple = (PyTuple) ((PyTuple) e.value).get(1);
				Integer line = (Integer) pyTuple.get(1);
				Integer column = (Integer) pyTuple.get(2);
				String lineString = (String) pyTuple.get(3);
				StringBuilder buf = new StringBuilder(lineString);
				if (lineString.length() >= column) {
					buf.insert(column - 1, "^");
				}
				return "Error occured\n" + " - Invalid Syntax Error on line "
						+ line + " / column " + column + "\n" + buf.toString();
			} catch (Exception ex) {
				LOG.error("Error occured while evludation PySyntaxError", ex);
				return "Error occured while evludation PySyntaxError";
			}
		}
		return null;

	}

}
