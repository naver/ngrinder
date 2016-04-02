package org.ngrinder.infra.plugin.finder;

import ro.fortsoft.pf4j.PluginClasspath;

/**
 * pf4j plugin path settings.
 *
 * @author Gisoo Gwon ,GeunWoo Son
 * @see https://github.com/decebals/pf4j
 * @since 3.0
 */
public class NGrinderPluginClasspath extends PluginClasspath {

	@Override
	protected void addResources() {
		classesDirectories.add("");
		libDirectories.add("");
	}

}
