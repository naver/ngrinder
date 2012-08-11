package org.ngrinder;

import java.io.File;

import org.ngrinder.infra.AgentConfig;
import org.ngrinder.infra.AgentHome;

public class MockAgentConfig extends AgentConfig {
	public static int counter = 0;

	@Override
	protected AgentHome resolveHome() {
		// TODO Auto-generated method stub
		AgentHome resolveHome = super.resolveHome();
		resolveHome = new AgentHome(new File(resolveHome.getDirectory(), "tmp_" + String.valueOf(counter++)));
		return resolveHome;
	}
}
