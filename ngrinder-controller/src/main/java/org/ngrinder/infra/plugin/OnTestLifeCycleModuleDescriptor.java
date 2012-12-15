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

import org.ngrinder.extension.OnTestLifeCycleRunnable;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ContainerManagedPlugin;

/**
 * Plugin Descriptor for {@link OnTestLifeCycleRunnable}.
 * 
 * The plugin should be constructed by implementing {@link OnTestLifeCycleRunnable}
 * 
 * @author JunHo Yoon
 * @since 3.0
 */

@PluginDescriptor("on-test-start")
@SuppressWarnings("deprecation")
public class OnTestLifeCycleModuleDescriptor extends AbstractModuleDescriptor<OnTestLifeCycleRunnable> {
	public OnTestLifeCycleRunnable getModule() {
		return ((ContainerManagedPlugin) getPlugin()).getContainerAccessor().createBean(getModuleClass());
	}
}
