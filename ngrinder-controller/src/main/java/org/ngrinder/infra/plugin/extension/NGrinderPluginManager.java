package org.ngrinder.infra.plugin.extension;

import org.ngrinder.infra.config.Config;
import org.pf4j.ExtensionFactory;
import org.pf4j.JarPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * DefaultPluginManager extended class.
 *
 * @author Gisoo Gwon ,GeunWoo Son
 * @since 3.0
 */
@Component
public class NGrinderPluginManager extends JarPluginManager {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	public NGrinderPluginManager(Config config) {
		super(config.getHome().getPluginsDirectory().toPath());
	}

	@PostConstruct
	public void init() {
		applicationContext.getAutowireCapableBeanFactory().autowireBean(getExtensionFactory());
	}

	@Override
	protected ExtensionFactory createExtensionFactory() {
		return new NGrinderSpringExtensionFactory();
	}
}
