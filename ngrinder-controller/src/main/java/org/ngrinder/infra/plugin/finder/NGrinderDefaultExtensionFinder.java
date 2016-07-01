package org.ngrinder.infra.plugin.finder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ro.fortsoft.pf4j.DefaultExtensionFinder;
import ro.fortsoft.pf4j.ExtensionFinder;
import ro.fortsoft.pf4j.ExtensionWrapper;
import ro.fortsoft.pf4j.PluginManager;

import java.util.List;
import java.util.Set;

/**
 * DefaultExtensionFinder extended class.
 * Connect with Finder.
 *
 * @author Gisoo Gwon ,GeunWoo Son
 * @see https://github.com/decebals/pf4j-spring
 * @since 3.0
 */
public class NGrinderDefaultExtensionFinder implements ExtensionFinder {

	private NGrinderServiceProviderExtensionFinder finder;

	public NGrinderDefaultExtensionFinder(PluginManager pluginManager) {
		finder = new NGrinderServiceProviderExtensionFinder(pluginManager);
	}


	@Override
	public <T> List<ExtensionWrapper<T>> find(Class<T> type) {
		return finder.find(type);
	}

	@Override
	public Set<String> findClassNames(String pluginId) {
		return finder.findClassNames(pluginId);
	}
}
