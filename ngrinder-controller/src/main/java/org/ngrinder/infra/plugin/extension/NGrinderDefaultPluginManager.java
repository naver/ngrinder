package org.ngrinder.infra.plugin.extension;

import java.net.MalformedURLException;

import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.plugin.finder.NGrinderPluginClasspath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.DefaultPluginRepository;
import ro.fortsoft.pf4j.DevelopmentPluginClasspath;
import ro.fortsoft.pf4j.ExtensionFactory;
import ro.fortsoft.pf4j.ExtensionFinder;
import ro.fortsoft.pf4j.PluginClasspath;
import ro.fortsoft.pf4j.RuntimeMode;
import ro.fortsoft.pf4j.spring.SpringExtensionFactory;
import ro.fortsoft.pf4j.util.JarFileFilter;

/**
 * DefaultPluginManager extended class.
 *
 * @author Gisoo Gwon ,GeunWoo Son
 * @see https://github.com/decebals/pf4j
 * @since 3.0
 */
@Component
public class NGrinderDefaultPluginManager extends DefaultPluginManager {

	@Autowired
	public NGrinderDefaultPluginManager(Config config, ApplicationContext applicationContext) throws MalformedURLException {
		super(config.isClustered() ? config.getExHome().getPluginsCacheDirectory() : config.getHome().getPluginsCacheDirectory());
		super.pluginRepository = new DefaultPluginRepository(config.getHome().getPluginsDirectory(), new JarFileFilter());
	}

	@Autowired
	public void setExtensionFinder(ExtensionFinder extensionFinder) {
		super.extensionFinder = extensionFinder;
	}

	@Autowired
	public void setSpringExtensionFactory(SpringExtensionFactory extensionFactory) {
		super.extensionFactory = extensionFactory;
	}

	@Override
	protected PluginClasspath createPluginClasspath() {
		return new NGrinderPluginClasspath();
	}

	@Override
	protected ExtensionFactory createExtensionFactory() {
		// Disable the default Factory
		return null;
	}

	@Override
	protected ExtensionFinder createExtensionFinder() {
		// Disable the default finder
		return null;
	}

}
