package org.ngrinder.infra.config;

import java.io.File;

import org.ngrinder.infra.AgentConfig;
import org.ngrinder.infra.AgentHome;

public class MockAgentConfigInControllerSide extends AgentConfig {
	public static int counter = 0;

	@Override
	protected AgentHome resolveHome() {
		AgentHome resolveHome = super.resolveHome();
		resolveHome = new AgentHome(new File(resolveHome.getDirectory(), "tmp_" + String.valueOf(counter++)));
		return resolveHome;
	}
}
