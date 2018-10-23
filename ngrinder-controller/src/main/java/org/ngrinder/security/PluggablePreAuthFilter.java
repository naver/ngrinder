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

import org.ngrinder.extension.OnPreAuthServletFilter;
import org.ngrinder.infra.plugin.PluginManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.*;
import java.io.IOException;
import java.util.List;

/**
 * Proxy filter which run combined preauth plugins.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
public class PluggablePreAuthFilter implements Filter {

	private CompositeFilter compositeFilter = new CompositeFilter();

	/**
	 * load the servlet filter plugins.
	 */
	public void loadPlugins(PluginManager pluginManager) {
		List<OnPreAuthServletFilter> enabledModulesByClass = pluginManager.getEnabledModulesByClass(OnPreAuthServletFilter.class);
		this.compositeFilter.setFilters(enabledModulesByClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
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
