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

import net.grinder.communication.CommunicationException;
import net.grinder.engine.communication.AgentDownloadGrinderMessage;
import net.grinder.engine.communication.AgentUpdateGrinderMessage;
import net.grinder.util.VersionNumber;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.ngrinder.common.util.CompressionUtil;
import org.ngrinder.common.util.ThreadUtil;
import org.ngrinder.infra.AgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Agent Update Message Handler.
 *
 * @author JunHo Yoon
 * @since 3.1
 */
public class AgentUpdateHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentUpdateHandler.class);

	private final AgentConfig agentConfig;
	private AgentController.ConsoleCommunication consoleCommunication;

	/**
	 * Agent Update handler.
	 *
	 * @param agentConfig agentConfig
	 */
	public AgentUpdateHandler(AgentConfig agentConfig, AgentController.ConsoleCommunication consoleCommunication) {
		LOGGER.info("AgentUpdateHandler is initialing !");
		this.consoleCommunication = consoleCommunication;
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
	 * @param message message to be sent
	 */
	public void updateAgent(AgentUpdateGrinderMessage message) throws CommunicationException {
		if (!isNewer(message.getVersion(), agentConfig.getInternalProperty("ngrinder.version", "UNKNOWN"))) {
			LOGGER.info("Update request was sent. But the old version was sent");
			return;
		}
		File download = new File(agentConfig.getHome().getTempDirectory(), "ngrinder-agent.tar.gz");
		int offset = 0;
		while (true) {

			AgentUpdateGrinderMessage updateMessage = (AgentUpdateGrinderMessage) consoleCommunication.sendBlockingMessage(new AgentDownloadGrinderMessage(message.getVersion(), offset));
			if (updateMessage.getNext() == -1) {
				break;
			}

			if (updateMessage.getNext() != -1 && updateMessage.getNext() == updateMessage.getBinary().length + offset) {

				OutputStream agentPackage = null;
				try {
					agentPackage = new FileOutputStream(download);
					IOUtils.write(updateMessage.getBinary(), agentPackage);
					offset += updateMessage.getBinary().length;
				} catch (Exception e) {
					LOGGER.error("Error while writing agent package,its offset is {} and details {}:", offset, e.getMessage());
				} finally {
					IOUtils.closeQuietly(agentPackage);
				}
			}

			// Sleep to let the other messages to be sent
			ThreadUtil.sleep(10);
		}
		try {
			File interDir = new File(agentConfig.getHome().getTempDirectory(), "update_package_unzip");
			File updateDir = new File(agentConfig.getCurrentDirectory(), "update_package");
			decompressDownloadPackage(download, interDir, updateDir);
			System.exit(0);
		} catch (Exception e) {
			LOGGER.error("Update request was sent. But download was failed {} ", e.getMessage());
			LOGGER.info("Details : ", e);
		}
	}

	private String createDownloadURL(String downloadUrl, String consoleIP) {
		return "";
	}

	void decompressDownloadPackage(File from, File interDir, File toDir) {
		interDir.mkdirs();
		toDir.mkdirs();

		if (FilenameUtils.isExtension(from.getName(), "gz")) {
			File outFile = new File(toDir, "ngrinder-agent.tar");
			CompressionUtil.ungzip(from, outFile);
			CompressionUtil.untar(outFile, interDir);
			FileUtils.deleteQuietly(outFile);
		} else {
			LOGGER.error("{} is not allowed to be unzipped.", from.getName());
		}

		try {
			FileUtils.deleteQuietly(toDir);
			FileUtils.moveDirectory(new File(interDir, "ngrinder-agent"), toDir);
		} catch (IOException e) {
			LOGGER.error("Error while moving a file ", e);
		}
		FileUtils.deleteQuietly(from);
		FileUtils.deleteQuietly(interDir);
	}
}
