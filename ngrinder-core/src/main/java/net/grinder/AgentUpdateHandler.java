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
import net.grinder.engine.communication.AgentUpdateGrinderMessage;
import net.grinder.util.VersionNumber;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.ngrinder.common.constants.AgentConstants;
import org.ngrinder.common.util.CompressionUtils;
import org.ngrinder.infra.AgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static org.ngrinder.common.constants.InternalConstants.PROP_INTERNAL_NGRINDER_VERSION;
import static org.ngrinder.common.util.Preconditions.checkTrue;

/**
 * Agent Update Message Handler.
 *
 * @author JunHo Yoon
 * @since 3.1
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class AgentUpdateHandler implements Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentUpdateHandler.class);

	private final AgentConfig agentConfig;
	private File download;
	@SuppressWarnings("FieldCanBeLocal")
	private int offset = 0;
	private FileOutputStream agentOutputStream;

	/**
	 * Agent Update handler.
	 *
	 * @param agentConfig agentConfig
	 * @param message     AgentUpdateGrinderMessage
	 */
	public AgentUpdateHandler(AgentConfig agentConfig, AgentUpdateGrinderMessage message)
			throws FileNotFoundException {
		if (!agentConfig.getAgentProperties().getPropertyBoolean(AgentConstants.PROP_AGENT_UPDATE_ALWAYS)) {
			checkTrue(isNewer(message.getVersion(), agentConfig.getInternalProperties().getProperty(PROP_INTERNAL_NGRINDER_VERSION)),
					"Update request was sent. But it's the older version " + message.getVersion());
		}
		this.agentConfig = agentConfig;
		this.download = new File(agentConfig.getHome().getTempDirectory(), "ngrinder-agent.tar");
		this.agentOutputStream = new FileOutputStream(download);
		LOGGER.info("AgentUpdateHandler is initialized!");
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
		FileUtils.deleteQuietly(download);
	}

	/**
	 * Update agent based on the current message.
	 *
	 * @param message message to be sent
	 */
	public void update(AgentUpdateGrinderMessage message) throws CommunicationException {
		if (message.getOffset() != offset) {
			throw new CommunicationException("Expected " + offset + " offset," +
					" but " + message.getOffset() + " was sent. " + ToStringBuilder.reflectionToString(message));
		}
		try {
			IOUtils.write(message.getBinary(), agentOutputStream);
			offset = message.getNext();
		} catch (IOException e) {
			throw new CommunicationException("Error while writing binary", e);
		}
		if (message.getNext() == 0) {
			IOUtils.closeQuietly(agentOutputStream);
			decompressDownloadPackage();
			// Then just exist to run the agent update process.
			System.exit(0);
		}
	}

	void decompressDownloadPackage() {
		File interDir = new File(agentConfig.getHome().getTempDirectory(), "update_package_unpacked");
		File toDir = new File(agentConfig.getCurrentDirectory(), "update_package");
		interDir.mkdirs();
		toDir.mkdirs();

		if (FilenameUtils.isExtension(download.getName(), "tar")) {
			File outFile = new File(toDir, "ngrinder-agent.tar");
			CompressionUtils.untar(download, interDir);
			FileUtils.deleteQuietly(outFile);
		} else {
			LOGGER.error("{} is not allowed to be unpacked.", download.getName());
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
