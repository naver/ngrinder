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
package org.ngrinder.common.constants;

/**
 * Agent related constants.
 *
 * @since 3.3
 */
public interface AgentConstants {
	String PROP_AGENT_ALL_LOGS = "agent.all_logs";
	String PROP_AGENT_KEEP_LOGS = "agent.keep_logs";
	String PROP_AGENT_UPDATE_ALWAYS = "agent.update_always";
	String PROP_AGENT_CONTROLLER_HOST = "agent.controller_host";
	String PROP_AGENT_CONTROLLER_PORT = "agent.controller_port";
	String PROP_AGENT_HOST_ID = "agent.host_id";
	String PROP_AGENT_JAVA_OPT = "agent.java_opt";
	String PROP_AGENT_JVM_CLASSPATH = "agent.jvm.classpath";
	String PROP_AGENT_LIMIT_XMX = "agent.limit_xmx";
	String PROP_AGENT_SUBREGION = "agent.subregion";
	String PROP_AGENT_OWNER = "agent.owner";
	String PROP_AGENT_SERVER_MODE = "agent.server_mode";
	String PROP_AGENT_CONNECTION_MODE = "agent.connection_mode";
	String PROP_AGENT_CONNECTION_PORT = "agent.connection_port";
	String PROP_AGENT_BROADCAST_IP = "agent.broadcast_ip";
	String PROP_AGENT_ENABLE_LOCAL_DNS = "agent.enable_local_dns";
	String PROP_AGENT_ENABLE_SECURITY = "agent.enable_security";

	String VALUE_AGENT_TO_CONTROLLER = "agent_to_controller";
	String VALUE_CONTROLLER_TO_AGENT = "controller_to_agent";

	// Deprecated, agent region is automatically set to the controller region which the agent is connected.
	String PROP_AGENT_REGION = "agent.region";
}
