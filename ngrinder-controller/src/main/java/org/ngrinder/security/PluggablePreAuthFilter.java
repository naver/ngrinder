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
package org.ngrinder.security;

import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import org.ngrinder.infra.plugin.OnPreAuthServletFilterModuleDescriptor;
import org.ngrinder.infra.plugin.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.CompositeFilter;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import java.io.IOException;
import java.util.List;

/**
 * Proxy filter which run combined preauth plugins.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@Profile("production")
@Component("pluggablePreAuthFilter")
public class PluggablePreAuthFilter implements Filter {
	@Autowired
	private PluginManager pluginManager;

	private CompositeFilter compositeFilter;

	/**
	 * Initialize the servlet filter plugins.
	 *
	 * @throws ServletException
	 */
	@PostConstruct
	public void init() {
		this.compositeFilter = new CompositeFilter();
		pluginInit();
		pluginManager.addPluginUpdateEvent(this);
	}

	/**
	 * Initialize plugins.
	 */
	protected void pluginInit() {
		List<Filter> enabledModulesByClass = pluginManager.getEnabledModulesByDescriptorAndClass(
				OnPreAuthServletFilterModuleDescriptor.class, Filter.class);
		this.compositeFilter.setFilters(enabledModulesByClass);
	}

	/**
	 * Event handler for plugin enable.
	 *
	 * @param event event
	 */
	@PluginEventListener
	public void onPluginEnabled(PluginEnabledEvent event) {
		pluginInit();
	}

	/**
	 * Event handler for plugin disable.
	 *
	 * @param event event
	 */
	@PluginEventListener
	public void onPluginDisabled(PluginDisabledEvent event) {
		pluginInit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException,
			ServletException {
		this.compositeFilter.doFilter(request, response, chain);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		this.compositeFilter.destroy();
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.compositeFilter.init(filterConfig);
	}
}
