package org.ngrinder.infra.plugin.extension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PluginWrapper;
import ro.fortsoft.pf4j.spring.SpringExtensionFactory;
import ro.fortsoft.pf4j.spring.SpringPlugin;

/**
 * SpringExtensionFactory extended class.
 * The springframework ApplicationContext injection.
 *
 * @author Gisoo Gwon ,GeunWoo Son
 * @see https://github.com/decebals/pf4j-spring
 * @since 3.0
 */
@Component
public class NGrinderSpringExtensionFactory extends SpringExtensionFactory {

	private final PluginManager pluginManager;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	public NGrinderSpringExtensionFactory(PluginManager pluginManager) {
		super(pluginManager);
		this.pluginManager = pluginManager;
	}

	protected void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public Object create(Class<?> extensionClass) {
		Object extension = createWithoutSpring(extensionClass);
		if (extension != null) {
			PluginWrapper pluginWrapper = pluginManager.whichPlugin(extensionClass);
			if (pluginWrapper != null) {
				applicationContext.getAutowireCapableBeanFactory().autowireBean(extension);
			}
		}
		return extension;
	}

}
