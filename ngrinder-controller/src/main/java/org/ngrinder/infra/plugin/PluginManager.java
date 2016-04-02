/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.infra.plugin;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.ngrinder.infra.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import ro.fortsoft.pf4j.DefaultPluginManager;

/**
 * Plugin manager which is responsible to initialize the plugin infra.<br/>
 * It is built on Plugin Framework for Java.
 *
 * @author JunHo Yoon ,GeunWoo Son
 * @see https://github.com/decebals/pf4j
 * @since 3.0
 */
@Profile("production")
@Component
public class PluginManager {

	@Autowired
	private Config config;

	@Autowired
	private DefaultPluginManager manager;

	/**
	 * Initialize plugin component.
	 */
	@PostConstruct
	public void init() {
		// In case of test mode, no plugin is supported.
		if (isPluginSupportEnabled()) {
			initPluginFramework();
		}
	}

	/**
	 * Initialize Plugin Framework.
	 */
	public void initPluginFramework() {
		manager.loadPlugins();
		manager.startPlugins();
	}

	/**
	 * Check if plugin support is enabled.
	 *
	 * @return true if plugin is supported.
	 */
	protected boolean isPluginSupportEnabled() {
		return config.isPluginSupported();
	}

	/**
	 * Get plugins by module class.
	 *
	 * @param <M>         module type
	 * @param moduleClass model type class
	 * @return plugin list
	 */
	public <M> List<M> getEnabledModulesByClass(Class<M> moduleClass) {
		return getEnabledModulesByClass(moduleClass, null);
	}

	/**
	 * Get plugins by module class.
	 * <p/>
	 * This method puts the given default plugin at a head of returned plugin list.
	 *
	 * @param <M>           module type
	 * @param moduleClass   module class
	 * @param defaultPlugin default plugin
	 * @return plugin list
	 */
	public <M> List<M> getEnabledModulesByClass(Class<M> moduleClass, M defaultPlugin) {
		ArrayList<M> pluginClasses = new ArrayList<M>();
		if (defaultPlugin != null) {
			pluginClasses.add(defaultPlugin);
		}
		pluginClasses.addAll(manager.getExtensions(moduleClass));
		return pluginClasses;
	}

}
