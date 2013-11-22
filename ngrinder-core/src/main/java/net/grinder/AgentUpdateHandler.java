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
import net.grinder.engine.communication.AgentControllerServerListener;
import net.grinder.engine.communication.AgentUpdateGrinderMessage;
import net.grinder.util.VersionNumber;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.ngrinder.common.util.CompressionUtil;
import org.ngrinder.infra.AgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static org.ngrinder.common.util.Preconditions.checkNotZero;
import static org.ngrinder.common.util.Preconditions.checkTrue;

/**
 * Agent Update Message Handler.
 *
 * @author JunHo Yoon
 * @since 3.1
 */
public class AgentUpdateHandler implements Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentUpdateHandler.class);

	private final AgentConfig agentConfig;
	private AgentController.ConsoleCommunication consoleCommunication;
	private AgentControllerServerListener messageListener;
	private File download;
	private int offset = 0;
	private FileOutputStream agentOutputStream;

	/**
	 * Agent Update handler.
	 *
	 * @param agentConfig agentConfig
	 */
	public AgentUpdateHandler(AgentConfig agentConfig, AgentUpdateGrinderMessage message,
	                          AgentController.ConsoleCommunication consoleCommunication)
			throws FileNotFoundException {
		checkTrue(isNewer(message.getVersion(), agentConfig.getInternalProperty("ngrinder.version",
				"UNKNOWN")), "Update request was sent. But the old version was sent");

		this.consoleCommunication = consoleCommunication;
		this.agentConfig = agentConfig;
		this.offset = 0;
		this.download = new File(agentConfig.getHome().getTempDirectory(), "ngrinder-agent.tar");
		this.agentOutputStream = new FileOutputStream(download);
		LOGGER.info("AgentUpdateHandler is initialized !");
	}

	boolean isNewer(String newVersion, String installedVersion) {
		if (newVersion.contains("-SNAPSHOT")) {
			return true;
		}
		installedVersion = installedVersion.replaceAll("\\(.*\\)", "").trim();
		newVersion = newVersion.replaceAll("\\(.*\\)", "").trim();
		return new VersionNumber(newVersion).compareTo(new VersionNumber(installedVersion)) > 0;
	}

	public void close() {
		IOUtils.closeQuietly(agentOutputStream);
	}

	/**
	 * Update agent based on the current message.
	 *
	 * @param message message to be sent
	 */
	public void updateAgent(AgentUpdateGrinderMessage message) throws CommunicationException, IOException {
		checkNotZero(message.getNext(), "Consequent initial agent update grinder message was sent");
		if (message.getNext() != -1 && message.getNext() == message.getBinary().length +
				offset) {
			IOUtils.write(message.getBinary(), agentOutputStream);
		} else if (message.getNext() == -1) {
			decompressDownloadPackage();
			System.exit(0);
		} else {
			throw new CommunicationException("Wrong offset received from controller !");
		}
	}

	void decompressDownloadPackage() {
		File interDir = new File(agentConfig.getHome().getTempDirectory(), "update_package_unzip");
		File toDir = new File(agentConfig.getCurrentDirectory(), "update_package");
		interDir.mkdirs();
		toDir.mkdirs();

		if (FilenameUtils.isExtension(download.getName(), "tar")) {
			File outFile = new File(toDir, "ngrinder-agent.tar");
			CompressionUtil.untar(download, interDir);
			FileUtils.deleteQuietly(outFile);
		} else {
			LOGGER.error("{} is not allowed to be unzipped.", download.getName());
		}

		try {
			FileUtils.deleteQuietly(toDir);
			FileUtils.moveDirectory(new File(interDir, "ngrinder-agent"), toDir);
		} catch (IOException e) {
			LOGGER.error("Error while moving a file ", e);
		}
		FileUtils.deleteQuietly(download);
		FileUtils.deleteQuietly(interDir);
	}
}
