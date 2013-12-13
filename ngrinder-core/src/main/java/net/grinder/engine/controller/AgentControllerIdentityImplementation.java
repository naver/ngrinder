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
package net.grinder.engine.controller;

import net.grinder.common.processidentity.AgentIdentity;

/**
 * Agent Controller identity.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class AgentControllerIdentityImplementation extends AbstractAgentControllerIdentityImplementation implements
				AgentIdentity {

	private static final long serialVersionUID = 2;

	private int m_number = -1;

	private final String ip;

	private String region;

	/**
	 * Constructor.
	 * 
	 * @param name	The public name of the agent.
	 * @param ip	The ip of agent controller
	 */
	public AgentControllerIdentityImplementation(String name, String ip) {
		super(name);
		this.ip = ip;
	}

	/**
	 * Return the console allocated agent number.
	 * 
	 * @return The number.
	 */
	public int getNumber() {
		return m_number;
	}

	/**
	 * Set the console allocated agent number.
	 * 
	 * @param number	The number.
	 */
	public void setNumber(int number) {
		m_number = number;
	}

	/**
	 * Get ip.
	 * 
	 * @return ip
	 */
	public String getIp() {
		return ip;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

}
