package org.ngrinder.infra.config;

import java.util.Properties;

import org.ngrinder.common.model.Home;
import org.ngrinder.common.util.PropertiesWrapper;
import org.ngrinder.infra.annotation.TestOnlyComponent;

@TestOnlyComponent
public class MockConfig extends Config {
	private PropertiesWrapper wrapper = new PropertiesWrapper(new Properties());

	public void setSystemProperties(PropertiesWrapper wrapper) {
		this.wrapper = wrapper;
	}

	@Override
	public PropertiesWrapper getSystemProperties() {
		return wrapper;
	}

	@Override
	public void loadSystemProperties() {
		super.loadSystemProperties();
		setSystemProperties(super.getSystemProperties());
	}

}
