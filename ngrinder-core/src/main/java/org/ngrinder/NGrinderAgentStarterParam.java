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
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.ngrinder.common.constants.CommonConstants;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.ngrinder.common.constants.AgentConstants.*;

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

	public NGrinderModeParam getModeParam() {
		return NGrinderModeParam.valueOf(mode);
	}

	/**
	 * Starter parameters.
	 *
	 * @since 3.3
	 */
	@Parameters(separators = "= ")
	public enum NGrinderModeParam {
		agent("run_agent") {

			@Parameter(names = {"-ch", "--controller-host"}, required = false, description = "controller host or ip.")
			public String controllerHost = null;

			@Parameter(names = {"-cp", "--controller-port"}, required = false, description = "controller port.")
			public Integer controllerPort = null;

			@Parameter(names = {"-r", "--region"}, required = false, description = "region")
			public String region = null;


			@Parameter(names = {"-hi", "--host-id"}, required = false, description = "this agent's unique host id")
			public String hostId = null;


			@Override
			protected void processInternal() {
				if (controllerHost != null) {
					System.setProperty(PROP_AGENT_CONTROLLER_HOST, controllerHost);
				}

				if (controllerPort != null) {
					System.setProperty(PROP_AGENT_CONTROLLER_PORT, controllerPort.toString());
				}

				if (hostId != null) {
					System.setProperty(PROP_AGENT_HOST_ID, hostId);
				}

				if (region != null) {
					System.setProperty(PROP_AGENT_REGION, region);
				}
			}
		},
		monitor("run_monitor") {
			@Override
			protected void processInternal() {
				// Do nothing
			}
		};

		@Parameter(names = {"-o", "--overwrite-config"}, required = false,
				description = "overwrite overwrite the existing .ngrinder_agent/agent.conf with the local __agent.conf")
		public Boolean overwriteConfig = null;


		@Parameter(names = {"-ah", "--agent-home"}, required = false,
				description = "this agent's unique home path. The default is ~/.ngrinder_agent")
		public String agentHome = null;


		@Parameter(names = {"-s", "--silent"}, description = "silent mode")
		public Boolean silent = null;

		@Parameter(names = {"-v", "--version"}, description = "show version")
		public Boolean version = null;


		@DynamicParameter(names = "-D", description = "dynamic parameters", hidden = true)
		public Map<String, String> params = new HashMap<String, String>();


		@SuppressWarnings("FieldCanBeLocal")
		@Parameter(names = {"-help", "-?", "-h"}, description = "prints this message")
		public Boolean help = null;

		private JCommander commander;

		NGrinderModeParam(String programName) {
			this.commander = new JCommander(NGrinderModeParam.this);
			commander.setProgramName(programName + (File.separator.equals("/") ? ".sh" : ".bat"));
		}

		public void parse(String[] args) {
			try {
				commander.parse(args);
				process();
			} catch (Exception e) {
				System.err.println("[Configuration Error]");
				System.err.println(e.getMessage());
				usage();
				System.exit(-1);
			}
		}

		public void process() {
			if (agentHome != null) {
				System.setProperty("ngrinder.agent.home", agentHome);
			}

			if (overwriteConfig != null) {
				System.setProperty(CommonConstants.PROP_OVERWRITE_CONFIG, "true");
			}

			if (silent != null) {
				System.setProperty(CommonConstants.PROP_COMMON_SILENT_MODE, "true");
			}

			processInternal();
		}

		protected abstract void processInternal();

		public void usage() {
			commander.usage();
		}
	}

}
