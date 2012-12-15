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

import java.io.Serializable;

import net.grinder.util.UniqueIdentityGenerator;

/**
 * Custom agent controller identity implementation.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@SuppressWarnings("serial")
public abstract class AbstractAgentControllerIdentityImplementation implements Serializable {

	private static final UniqueIdentityGenerator IDENTITY_GENERATOR = new UniqueIdentityGenerator();

	private final String m_identity;
	private String m_name;

	protected AbstractAgentControllerIdentityImplementation(String name) {
		m_identity = IDENTITY_GENERATOR.createUniqueString(name);
		m_name = name;
	}

	/**
	 * Return the process name.
	 * 
	 * @return The process name.
	 */
	public final String getName() {
		return m_name;
	}

	/**
	 * Allows the public process name to be changed.
	 * 
	 * @param name
	 *            The new process name.
	 */
	public void setName(String name) {
		m_name = name;
	}

	/**
	 * Implement equality semantics. We compare equal to all copies of ourself, but nothing else.
	 * 
	 * @return The hash code.
	 */
	public final int hashCode() {
		return m_identity.hashCode();
	}

	/**
	 * Implement equality semantics. We compare equal to all copies of ourself, but nothing else.
	 * 
	 * @param o
	 *            Object to compare.
	 * @return <code>true</code> => its equal.
	 */
	public final boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		// instanceof does not break symmetry since equals() is final.
		if (!(o instanceof AbstractAgentControllerIdentityImplementation)) {
			return false;
		}

		final String otherIdentity = ((AbstractAgentControllerIdentityImplementation) o).m_identity;

		return m_identity.equals(otherIdentity) && getClass().equals(o.getClass());
	}

	/**
	 * String representation.
	 * 
	 * @return A string representation of this process identity.
	 */
	public final String toString() {
		return "Process '" + m_name + "' [" + m_identity + "]";
	}
}
