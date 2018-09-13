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
package org.ngrinder.infra;


import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Agent Configuration Test.
 *
 * @author Mavlarn
 */
public class AgentConfigTest {

	/**
	 * Test method for {@link org.ngrinder.infra.AgentConfig#init()}.
	 *
	 * @throws IOException
	 */
	@Test
	@Ignore
	public void testAgentConfigInitialization() throws IOException {
		// Given
		AgentConfig config = new AgentConfig();
		config.init();
		File homeDir = config.getHome().getDirectory();
		// When
		config.saveAgentPidProperties("1000", "agent");
		String pid = config.getAgentPidProperties("agent");
		// Then
		assertThat(pid).isEqualTo("1000");

		//  When
		config.saveAgentPidProperties("1001", "monitor");
		String monitorPid = config.getAgentPidProperties("monitor");

		// Then
		assertThat(monitorPid).isEqualTo("1001");
		assertThat(config.isDevMode()).isEqualTo(false);

		// When
		System.setProperty("ngrinder.agent.home", "./tmp_agent_home");
		config.init();
		// Then
		homeDir = config.getHome().getDirectory();
		assertThat(homeDir.getAbsolutePath()).contains(File.separator + "tmp_agent_home");
		FileUtils.deleteDirectory(homeDir);
		System.setProperty("ngrinder.agent.home", "");

	}

}
