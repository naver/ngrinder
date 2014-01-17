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
@Parameters(separators = "= ")
public class NGrinderAgentStarterParam {

	@Parameter(names = {"-m", "-mode", "--mode"}, required = false,
			description = "run mode. The agent/monitor modes are available.", hidden = true)
	public String mode = null;

	@Parameter(names = {"-c", "-command", "--command"}, required = false, description = "execution command. " +
			"The stop/start command are available.", hidden = true)
	public String command = "start";

	@Parameter(names = {"-o", "--overwrite-config"}, required = false,
			description = "overwrite overwrite the existing .ngrinder_agent/agent.conf with the local __agent.conf")
	public Boolean overwriteConfig = null;

	@Parameter(names = {"-ah", "--agent-home"}, required = false,
			description = "this agent's unique home path. The default is ~/.ngrinder_agent")
	public String agentHome = null;


	@Parameter(names = {"-ch", "--controller-host"}, required = false, description = "controller host or ip.")
	public String controllerHost = null;

	@Parameter(names = {"-cp", "--controller-port"}, required = false, description = "controller port.")
	public Integer controllerPort = null;

	@Parameter(names = {"-r", "--region"}, required = false, description = "region")
	public String region = null;


	@Parameter(names = {"-hi", "--host-id"}, required = false, description = "this agent's unique host id")
	public String hostId = null;

	@SuppressWarnings("FieldCanBeLocal")
	@Parameter(names = {"-help", "-?", "-h"}, description = "prints this message")
	public Boolean help = null;

	@Parameter(names = {"-s", "--silent"}, description = "silent mode")
	public Boolean silent = null;

	@Parameter(names = {"-v", "--version"}, description = "show version")
	public Boolean version = null;

	@DynamicParameter(names = "-D", description = "dynamic parameters", hidden = true)
	public Map<String, String> params = new HashMap<String, String>();
}
