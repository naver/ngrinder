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
package org.ngrinder.infra.config;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.model.Home;
import org.ngrinder.common.util.PropertiesWrapper;
import org.ngrinder.infra.init.CacheClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.util.ReflectionTestUtils;

@ContextConfiguration("classpath:applicationContext.xml")
public class ConfigTest extends AbstractJUnit4SpringContextTests implements NGrinderConstants {

	@Autowired
	private MockConfig config;
	
	@Autowired
	private CacheClusterService clusterInit;

	@Autowired
	private EhCacheCacheManager cacheManager;

	@Test
	public void testDefaultHome() {
		Home home = config.getHome();
		System.out.println(home);
		File ngrinderHomeUnderUserHome = new File(System.getProperty("user.home"), ".ngrinder");
		assertThat(home.getDirectory(), is(ngrinderHomeUnderUserHome));
		assertThat(home.getPluginsDirectory(), is(new File(ngrinderHomeUnderUserHome, "plugins")));
	}

	@Test
	public void testTestMode() {
		PropertiesWrapper wrapper = mock(PropertiesWrapper.class);
		config.setSystemProperties(wrapper);
		// When testmode false and pluginsupport is true, it should be true
		when(wrapper.getPropertyBoolean("testmode", false)).thenReturn(false);
		when(wrapper.getPropertyBoolean("pluginsupport", true)).thenReturn(false);
		assertThat(config.isPluginSupported(), is(false));

		// When testmode true and pluginsupport is false, it should be false
		when(wrapper.getPropertyBoolean("testmode", false)).thenReturn(true);
		when(wrapper.getPropertyBoolean("pluginsupport", true)).thenReturn(false);
		assertThat(config.isPluginSupported(), is(false));

		// When testmode false and pluginsupport is false, it should be false
		when(wrapper.getPropertyBoolean("testmode", false)).thenReturn(false);
		when(wrapper.getPropertyBoolean("pluginsupport", true)).thenReturn(false);
		assertThat(config.isPluginSupported(), is(false));

		// When testmode true and pluginsupport is true, it should be false
		when(wrapper.getPropertyBoolean("testmode", false)).thenReturn(true);
		when(wrapper.getPropertyBoolean("pluginsupport", true)).thenReturn(true);
		assertThat(config.isPluginSupported(), is(false));
		
		when(wrapper.getPropertyBoolean("testmode", false)).thenReturn(true);
		when(wrapper.getPropertyBoolean("security", false)).thenReturn(true);
		assertThat(config.isSecurityEnabled(), is(false));
		
		when(wrapper.getPropertyBoolean("testmode", false)).thenReturn(false);
		when(wrapper.getPropertyBoolean("security", false)).thenReturn(true);
		assertThat(config.isSecurityEnabled(), is(true));
		
		when(wrapper.getPropertyBoolean("testmode", false)).thenReturn(false);
		when(wrapper.getPropertyBoolean("security", false)).thenReturn(false);
		assertThat(config.isSecurityEnabled(), is(false));
	}
	
	@Test
	public void testPolicyFileLoad() {
		String processAndThreadPolicyScript = config.getProcessAndThreadPolicyScript();
		assertThat(processAndThreadPolicyScript, containsString("function"));
	}
	
	@Test
	public void testVersionString() {
		String version = config.getVesion();
		assertThat(version, not("UNKNOWN"));
	}
	
	@Test
	public void testLoadClusterConfig() {
		PropertiesWrapper wrapper = mock(PropertiesWrapper.class);
		config.setSystemProperties(wrapper);
		when(wrapper.getPropertyInt(NGRINDER_PROP_CLUSTER_LISTENER_PORT, 40003)).thenReturn(40003);
		when(wrapper.getProperty(NGRINDER_PROP_CLUSTER_URIS, null)).thenReturn("");
		
		config.loadClusterConfig();
		assertThat(config.isCluster(), is(false));
		
		when(wrapper.getPropertyInt(NGRINDER_PROP_CLUSTER_LISTENER_PORT, 40003)).thenReturn(40003);
		when(wrapper.getProperty(NGRINDER_PROP_CLUSTER_URIS, null)).thenReturn("192.168.1.1;192.168.2.2;192.168.3.3");
		config.loadClusterConfig();
		assertThat(config.isCluster(), is(true));
	}
	
//	@Test
//	public void testIniTExtendConfig() {
//		config.loadExtendProperties();
//		clusterInit.initRegion();
//		Cache distCache = dynamicCacheManager.getCache(NGrinderConstants.CACHE_NAME_DISTRIBUTED_MAP);
//		@SuppressWarnings("unchecked")
//		List<String> regionList = (List<String>)distCache.get(NGrinderConstants.CACHE_NAME_REGION_LIST).get();
//		assertThat(regionList.size(), is(1));
//		assertThat(regionList.get(0), is(Config.NON_REGION));
//		
//		ReflectionTestUtils.setField(config, "region", "Beijing");
//		ReflectionTestUtils.setField(clusterInit, "config", config);
//		clusterInit.initRegion();
//		assertThat(regionList.size(), is(2));
//		assertThat(regionList.get(1), is("Beijing"));
//	}
}
