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
package org.ngrinder.service;

import org.ngrinder.common.util.PropertiesWrapper;

/**
 * Config access interface.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public interface IConfig {

	/**
	 * Check if it's test mode.
	 *
	 * @return true if test mode
	 */
	public abstract boolean isDevMode();

	/**
	 * Check if it's the security enabled mode.
	 *
	 * @return true if security is enabled.
	 */
	public abstract boolean isSecurityEnabled();

	/**
	 * Check if plugin support is enabled. The reason why we need this configuration is that it
	 * takes time to initialize plugin system in unit test context.
	 *
	 * @return true if plugin is supported.
	 */
	public abstract boolean isPluginSupported();

	/**
	 * Get the controller properties.
	 *
	 * @return {@link PropertiesWrapper} which is loaded from system.conf.
	 */
	public abstract PropertiesWrapper getControllerProperties();

	/**
	 * Get the cluster properties.
	 *
	 * @return {@link PropertiesWrapper} which is loaded from system.conf.
	 */
	public abstract PropertiesWrapper getClusterProperties();

	/**
	 * Get the system properties.
	 * This is only for backward compatibility. use #getControllerProperties().
	 * @return {@link PropertiesWrapper} which is loaded from system.conf.
	 * @deprecated use #getControllerProperties() instead
	 */
	public abstract PropertiesWrapper getSystemProperties();

}