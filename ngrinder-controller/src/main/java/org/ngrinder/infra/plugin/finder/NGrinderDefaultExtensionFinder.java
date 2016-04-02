package org.ngrinder.infra.plugin.finder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ro.fortsoft.pf4j.DefaultExtensionFinder;
import ro.fortsoft.pf4j.PluginManager;

/**
 * DefaultExtensionFinder extended class.
 * Connect with Finder.
 *
 * @author Gisoo Gwon ,GeunWoo Son
 * @see https://github.com/decebals/pf4j-spring
 * @since 3.0
 */
@Component
public class NGrinderDefaultExtensionFinder extends DefaultExtensionFinder{

	@Autowired
	public NGrinderDefaultExtensionFinder(PluginManager pluginManager) {
		super(pluginManager);
		finders.add(new NGrinderServiceProviderExtensionFinder(pluginManager));
	}

	@Override
	protected void addDefaults(PluginManager pluginManager) {
		// Disable the default ProviderExtensionFinder
	}

}
