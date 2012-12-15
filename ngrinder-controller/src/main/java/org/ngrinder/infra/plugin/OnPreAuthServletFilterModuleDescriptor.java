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

import javax.servlet.Filter;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ContainerManagedPlugin;

/**
 * Plugin Descriptor for PreAuth Filter.
 * 
 * In the
 * {@link Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)}
 * method, <br/>
 * the plugin should set
 * {@link org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken}
 * in the {@link org.springframework.security.core.context.SecurityContext}
 * 
 * @author JunHo Yoon
 * @since 3.0.2
 */
@PluginDescriptor("on-preauth-servletfilter")
@SuppressWarnings("deprecation")
public class OnPreAuthServletFilterModuleDescriptor extends AbstractModuleDescriptor<Filter> {
	public Filter getModule() {
		return ((ContainerManagedPlugin) getPlugin()).getContainerAccessor().createBean(getModuleClass());
	}
}
