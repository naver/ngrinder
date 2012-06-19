package com.nhncorp.ngrinder.grinder;

import net.grinder.common.processidentity.AgentIdentity;

public class NGrinderAgentIdentityImplementation implements AgentIdentity {

	private static final long serialVersionUID = 2;

	private int m_number = -1;
	private String m_name;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *      		The public name of the agent.
	 */
	public NGrinderAgentIdentityImplementation(String name) {
		m_name = name;
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
	 * @param number
	 *            The number.
	 */
	public void setNumber(int number) {
		m_number = number;
	}

	@Override
	public String getName() {
		return m_name;
	}

}
