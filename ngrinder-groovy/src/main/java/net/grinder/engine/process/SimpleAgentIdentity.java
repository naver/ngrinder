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
package net.grinder.engine.process;

import net.grinder.common.processidentity.AgentIdentity;
import net.grinder.util.UniqueIdentityGenerator;

/**
 * Simplified Agent Identity implementation which will be ignored in the unit test context.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
public class SimpleAgentIdentity implements AgentIdentity {
	/** UUID. */
	private static final long serialVersionUID = 2674072961464183737L;
	private static final UniqueIdentityGenerator s_identityGenerator = new UniqueIdentityGenerator();
	private final String m_identity;

	private String name;
	private int number;

	/**
	 * Constructor.
	 * 
	 * @param name		agent identity name
	 * @param number	agent number.
	 */
	public SimpleAgentIdentity(String name, int number) {
		super();
		this.name = name;
		this.number = number;
		m_identity = s_identityGenerator.createUniqueString(name);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getNumber() {
		return number;
	}

	@Override
	public String getUniqueID() {
		return m_identity;
	}

}
