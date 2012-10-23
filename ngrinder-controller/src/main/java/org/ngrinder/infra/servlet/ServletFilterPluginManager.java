package org.ngrinder.infra.servlet;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.ngrinder.infra.annotation.RuntimeOnlyComponent;
import org.ngrinder.infra.plugin.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.CompositeFilter;

import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;

/**
 * Proxy filter which run combined servlet plugins.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@RuntimeOnlyComponent("servletFilterPluginManager")
public class ServletFilterPluginManager implements Filter {
	@Autowired
	private PluginManager pluginManager;

	private CompositeFilter compositeFilter;

	/**
	 * Initialize the servlet filter plugins.
	 * @throws ServletException
	 */
	@PostConstruct
	public void init()  {
		this.compositeFilter = new CompositeFilter();
		pluginInit();
		pluginManager.addPluginUpdateEvent(this);
	}

	/**
	 * Initialize plugins.
	 * 
	 */
	protected void pluginInit() {
		this.compositeFilter.setFilters(pluginManager.getEnabledModulesByClass(Filter.class));
	}

	/**
	 * Event handler for plugin enable.
	 * @param event event 
	 */
	@PluginEventListener
	public void onPluginEnabled(PluginEnabledEvent event) {
		pluginInit();
	}

	/**
	 * Event handler for plugin disable.
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
	}
}
