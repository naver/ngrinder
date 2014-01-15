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
package org.ngrinder;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.HashMap;
import java.util.Map;

/**
 * Starter parameters.
 *
 * @since 3.3
 */
@Parameters(separators = "=")
public class NGrinderAgentStarterParam {

	@Parameter(names = "-mode", required = false,
			description = "run mode. The agent/monitor modes are available.", hidden = true)
	public String mode = null;

	@Parameter(names = "-command", required = false, description = "command. The stop/start command are " +
			"available.", hidden = true)
	public String command = "start";

	@Parameter(names = {"-overwrite-config", "-o"}, required = false,
			description = "Overwrite overwrite the existing .ngrinder_agent/agent.conf with the local __agent.conf")
	public boolean overwriteConfig = false;

	@Parameter(names = "-agent-home", required = false,
			description = "the agent home path. The default is ~/.ngrinder_agent")
	public String agentHome = null;

	@Parameter(names = "-controller-ip", required = false, description = "the controller ip.")
	public String controllerIP = null;

	@Parameter(names = "-controller-port", required = false, description = "the controller port.")
	public Integer controllerPort = null;

	@Parameter(names = "-region", required = false, description = "the region")
	public String region = null;


	@Parameter(names = "-host-id", required = false, description = "the agent host id.")
	public String hostId = null;

	@SuppressWarnings("FieldCanBeLocal")
	@Parameter(names = {"-help", "-?", "-h"}, description = "prints this message")
	public Boolean help = false;

	@DynamicParameter(names = "-D", description = "Dynamic parameters", hidden = true)
	public Map<String, String> params = new HashMap<String, String>();
}
