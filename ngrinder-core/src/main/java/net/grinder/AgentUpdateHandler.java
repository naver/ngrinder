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
package net.grinder;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import net.grinder.engine.communication.UpdateAgentGrinderMessage;
import net.grinder.util.NetworkUtil;
import net.grinder.util.VersionNumber;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.ngrinder.common.util.CompressionUtil;
import org.ngrinder.infra.AgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Agent Update Message Handler.
 * 
 * @author JunHo Yoon
 * @since 3.1
 */
public class AgentUpdateHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentUpdateHandler.class);

	private final AgentConfig agentConfig;

	/**
	 * Agent Update handler.
	 * 
	 * @param agentConfig
	 *            agentConfig
	 */
	public AgentUpdateHandler(AgentConfig agentConfig) {
		this.agentConfig = agentConfig;
	}

	boolean isNewer(String newVersion, String installedVersion) {
		installedVersion = installedVersion.replaceAll("\\(.*\\)", "").trim();
		newVersion = newVersion.replaceAll("\\(.*\\)", "").trim();
		return new VersionNumber(newVersion).compareTo(new VersionNumber(installedVersion)) > 0;
	}

	/**
	 * Update agent based on the current message.
	 * 
	 * @param message
	 *            message to be sent
	 */
	public void updateAgent(UpdateAgentGrinderMessage message) {
		if (!isNewer(message.getVersion(), agentConfig.getInternalProperty("ngrinder.version", "UNKNOWN"))) {
			LOGGER.info("Update request was sent. But the old version was sent");
			return;
		}
		File downloadFolder = new File(System.getProperty("user.dir"), "download");
		downloadFolder.mkdirs();
		File dest = new File(downloadFolder, message.getFileName());

		File interDir = new File(agentConfig.getCurrentDirectory(), "update_package_unzip");
		File updatePackageDir = new File(System.getProperty("user.dir"), "update_package");
		try {
			NetworkUtil.downloadFile(message.getDownloadUrl(), dest);
			decompress(dest, interDir, updatePackageDir);
			System.exit(10);
		} catch (Exception e) {
			LOGGER.error("Update request was sent. But download was failed {} ", e.getMessage());
			LOGGER.info("Details : ", e);
		}
	}

	void decompress(File from, File interDir, File toDir) {
		interDir.mkdirs();
		if (FilenameUtils.isExtension(from.getName(), "gz")) {
			File outFile = new File(toDir, "ngrinder-agent.tar");
			CompressionUtil.ungzip(from, outFile);
			CompressionUtil.untar(outFile, interDir);
			FileUtils.deleteQuietly(outFile);
		} else if (FilenameUtils.isExtension(from.getName(), "zip")) {
			CompressionUtil.unzip(from, interDir);
		} else {
			LOGGER.error("{} is not allowed to be unzipped.", from.getName());
		}
		// List up only directories.
		File[] listFiles = interDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return (pathname.isDirectory());
			}
		});
		if (ArrayUtils.isNotEmpty(listFiles)) {
			try {
				FileUtils.deleteQuietly(toDir);
				FileUtils.moveDirectory(listFiles[0], toDir);
			} catch (IOException e) {
				LOGGER.error("Error while moving a file ", e);
			}
		} else {
			LOGGER.error("{} is empty.", interDir.getName());
		}
		FileUtils.deleteQuietly(from);
		FileUtils.deleteQuietly(interDir);

	}
}
