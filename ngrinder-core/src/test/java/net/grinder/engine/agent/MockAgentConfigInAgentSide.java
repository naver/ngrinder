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
package net.grinder.engine.agent;

import java.io.File;

import org.ngrinder.infra.AgentConfig;
import org.ngrinder.infra.AgentHome;

public class MockAgentConfigInAgentSide extends AgentConfig {
	public static int counter = 0;

	@Override
	protected AgentHome resolveHome() {
		AgentHome resolveHome = super.resolveHome();
		resolveHome = new AgentHome(new File(resolveHome.getDirectory(), "tmp_" + String.valueOf(counter++)));
		return resolveHome;
	}
}
