package org.ngrinder.infra.plugin.extension;

import org.pf4j.ExtensionFactory;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * SpringExtensionFactory extended class.
 * The springframework ApplicationContext injection.
 *
 * @author Gisoo Gwon ,GeunWoo Son
 * @since 3.0
 */
public class NGrinderSpringExtensionFactory implements ExtensionFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(NGrinderSpringExtensionFactory.class);

	@Autowired
	private PluginManager pluginManager;

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public <T> T create(Class<T> extensionClass) {
		T extension = createWithoutSpring(extensionClass);
		if (extension != null) {
			PluginWrapper pluginWrapper = pluginManager.whichPlugin(extensionClass);
			if (pluginWrapper != null) {
				applicationContext.getAutowireCapableBeanFactory().autowireBean(extension);
			}
		}
		return extension;
	}

	private <T> T createWithoutSpring(Class<T> extensionClass) {
		try {
			return extensionClass.newInstance();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}

		return null;
	}
}
