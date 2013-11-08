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
package org.ngrinder.infra.config;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Properties;

import org.junit.Test;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.common.model.Home;
import org.ngrinder.common.util.PropertiesWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.util.ReflectionTestUtils;

@ContextConfiguration("classpath:applicationContext.xml")
public class ConfigTest extends AbstractJUnit4SpringContextTests implements NGrinderConstants {

	@Autowired
	private MockConfig config;

	@Test
	public void testDefaultHome() {
		Home home = config.getHome();
		File ngrinderHomeUnderUserHome = new File(System.getProperty("user.home"), ".ngrinder");
		assertThat(home.getDirectory(), is(ngrinderHomeUnderUserHome));
		assertThat(home.getPluginsDirectory(), is(new File(ngrinderHomeUnderUserHome, "plugins")));
	}
	
	@Test
	public void testGetMonitorPort() {
		int port = config.getMonitorPort();
		assertThat(port, not(0));
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
		String version = config.getVersion();
		String versionString = config.getVersion();
		assertThat(version, not("UNKNOWN"));
		assertThat(versionString, not("UNKNOWN"));
		config.initLogger(true);
		assertThat(config.isVerbose(), is(true));
	}

	@Test
	public void testLoadClusterConfig() {
		config.cluster = true;
		config.setRMIHostName();
	}

	@Test
	public void testLoadExtendProperties() {
		config.cluster = true;
		Properties wrapper = new Properties();
		wrapper.put(NGRINDER_PROP_REGION, "TestNewRegion");
		config.doRealOnRegion = true;
		// set mock exHome and test
		Home mockExHome = mock(Home.class);
		when(mockExHome.getProperties("system-ex.conf")).thenReturn(wrapper);
		when(mockExHome.exists()).thenReturn(true);
		ReflectionTestUtils.setField(config, "exHome", mockExHome);
		config.loadSystemProperties();
		assertThat(config.getRegion(), is("TestNewRegion"));
	}
}
