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
import net.grinder.common.processidentity.WorkerIdentity;

/**
 * Simplified {@link WorkerIdentity}y implementation which will be ignored in the unit test context.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
public class SimpleWorkerIdentity implements WorkerIdentity {

	private static final long serialVersionUID = 3;
	private int m_number;
	private SimpleAgentIdentity simpleAgentIdentity;

	/**
	 * Constructor.
	 * 
	 * @param name		worker name
	 * @param number	worker number
	 */
	public SimpleWorkerIdentity(String name, int number) {
		m_number = number;
		simpleAgentIdentity = new SimpleAgentIdentity(name, number);
	}

	public int getNumber() {
		return m_number;
	}

	@Override
	public String getName() {
		return "junit_runner";
	}

	/* (non-Javadoc)
	 * @see net.grinder.common.processidentity.WorkerIdentity#getAgentIdentity()
	 */
	@Override
	public AgentIdentity getAgentIdentity() {
		return simpleAgentIdentity;
	}
}