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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since
 */
public class AgentConfigTest {

	/**
	 * Test method for {@link org.ngrinder.infra.AgentConfig#init()}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testInit() throws IOException {
		AgentConfig config = new AgentConfig();
		config.init();
		File homeDir = config.getHome().getDirectory();
		System.out.println("Home:" + homeDir.getAbsolutePath());
		
		config.saveAgentPidProperties("1000", "agent");
		String pid  = config.getAgentPidProperties("agent");
		assertThat(pid, is("1000"));
		
		config.saveAgentPidProperties("1001", "monitor");
		String monitorPid  = config.getAgentPidProperties("monitor");
		assertThat(monitorPid, is("1001"));

		assertThat(config.isTestMode(), is(false));

		System.setProperty("ngrinder.agent.home", "./tmp_agent_home");
		config.init();
		homeDir = config.getHome().getDirectory();
		assertTrue(homeDir.getAbsolutePath().contains("tmp_agent_home"));
		FileUtils.deleteDirectory(homeDir);
	}

}
