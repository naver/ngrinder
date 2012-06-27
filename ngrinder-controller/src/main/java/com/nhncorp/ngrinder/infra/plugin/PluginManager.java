/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.nhncorp.ngrinder.infra.plugin;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;

import com.atlassian.plugin.DefaultModuleDescriptorFactory;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
import com.atlassian.plugin.main.AtlassianPlugins;
import com.atlassian.plugin.main.PluginsConfiguration;
import com.atlassian.plugin.main.PluginsConfigurationBuilder;
import com.atlassian.plugin.osgi.container.impl.DefaultPackageScannerConfiguration;
import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
import com.nhncorp.ngrinder.core.model.Home;
import com.nhncorp.ngrinder.infra.annotation.OnlyRuntimeComponent;
import com.nhncorp.ngrinder.infra.config.Config;

/**
 * Plugin manager which is responsible to init the plugin infra. It mainly uses
 * atlassian plugin framework.
 * 
 * @see https://developer.atlassian.com/display/PLUGINFRAMEWORK/Plugin+Framework
 * @author JunHo Yoon
 * @since 3.0
 */
@OnlyRuntimeComponent
public class PluginManager implements ServletContextAware {

	private static final Logger LOG = LoggerFactory.getLogger(PluginManager.class);

	private AtlassianPlugins plugins;
	private ServletContext servletContext;

	private static final int PLUGIN_UPDATE_FREQUENCY = 60;

	@Autowired
	private Config config;

	@PostConstruct
	public void init() {

		// waiting for sqlmap and grinder to start
		// Determine which packages to expose to plugins
		DefaultPackageScannerConfiguration scannerConfig = new DefaultPackageScannerConfiguration();
		scannerConfig.setServletContext(servletContext);
		scannerConfig.getPackageIncludes().add("com.nhncorp.*");
		scannerConfig.getPackageIncludes().add("org.apache.*");
		scannerConfig.getPackageIncludes().add("net.grinder.*");

		// Determine which module descriptors, or extension points, to expose.
		// This 'on-start' module is used throughout this guide as an example
		// only
		DefaultModuleDescriptorFactory modules = new DefaultModuleDescriptorFactory(new DefaultHostContainer());
		String packagename = "com.nhncorp";

		initPluginDescriptor(modules, packagename);

		// Determine which service objects to expose to plugins
		HostComponentProvider host = new HostComponentProvider() {
			public void provide(ComponentRegistrar reg) {
			}
		};
		Home home = config.getHome();

		// Construct the configuration
		PluginsConfiguration config = new PluginsConfigurationBuilder().pluginDirectory(home.getSubFile("plugins"))
				.packageScannerConfiguration(scannerConfig)
				.hotDeployPollingFrequency(PLUGIN_UPDATE_FREQUENCY, TimeUnit.SECONDS).hostComponentProvider(host)
				.moduleDescriptorFactory(modules).build();

		// Start the plugin framework
		plugins = new AtlassianPlugins(config);
		plugins.start();
		
		for (Runnable runnable : plugins.getPluginAccessor().getEnabledModulesByClass(Runnable.class)) {
			runnable.run();
		}
	}

	@SuppressWarnings("rawtypes")
	protected void initPluginDescriptor(DefaultModuleDescriptorFactory modules, String packagename) {
		final Reflections reflections = new Reflections(packagename);

		Set<Class<? extends AbstractModuleDescriptor>> pluginDescriptors = reflections
				.getSubTypesOf(AbstractModuleDescriptor.class);

		for (Class<? extends AbstractModuleDescriptor> pluginDescriptor : pluginDescriptors) {
			PluginDescriptor pluginDescriptorAnnotation = pluginDescriptor.getAnnotation(PluginDescriptor.class);
			if (pluginDescriptorAnnotation == null) {
				LOG.error("plugin descriptor " + pluginDescriptor.getName()
						+ " doesn't have PluginDescriptor annotation. Skip..");
			}
			if (StringUtils.isEmpty(pluginDescriptorAnnotation.value())) {
				LOG.error("plugin descriptor " + pluginDescriptor.getName()
						+ " doesn't have corresponding plugin key. Skip..");
			} else {
				modules.addModuleDescriptor(pluginDescriptorAnnotation.value(), pluginDescriptor);
				LOG.info("plugin descriptor " + pluginDescriptor.getName() + " with "
						+ pluginDescriptorAnnotation.value() + " is initiated.");
			}

		}
	}

	public <M> List<M> getEnabledModulesByClass(Class<M> moduleClass) {
		return plugins.getPluginAccessor().getEnabledModulesByClass(moduleClass);
	}

	@PreDestroy
	public void destroy() {
		plugins.stop();
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

}
