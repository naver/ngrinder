package org.ngrinder.infra.plugin;

import javax.annotation.PostConstruct;

import org.ngrinder.infra.annotation.TestOnlyComponent;

@TestOnlyComponent
public class MockPluginManager extends PluginManager {

	@PostConstruct
	public void init() {

	}

	@Override
	protected boolean isPluginSupportEnabled() {
		return true;
	}
}
