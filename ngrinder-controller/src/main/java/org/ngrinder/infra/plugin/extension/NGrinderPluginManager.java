package org.ngrinder.infra.plugin.extension;

import org.ngrinder.infra.config.Config;
import org.pf4j.ExtensionFactory;
import org.pf4j.JarPluginManager;
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

	private final ApplicationContext applicationContext;

	public NGrinderPluginManager(Config config, ApplicationContext applicationContext) {
		super(config.getHome().getPluginsDirectory().toPath());
		this.applicationContext = applicationContext;
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
