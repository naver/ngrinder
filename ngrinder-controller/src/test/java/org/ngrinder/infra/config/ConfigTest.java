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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;
import org.ngrinder.common.model.Home;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration("classpath:applicationContext.xml")
public class ConfigTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	public Config config;

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
		Config spiedConfig = spy(config);
		// When testmode false and pluginsupport is true, it should be true
		when(spiedConfig.getSystemProperty("testmode", "false")).thenReturn("false");
		when(spiedConfig.getSystemProperty("pluginsupport", "true")).thenReturn("false");
		assertThat(spiedConfig.isPluginSupported(), is(true));

		// When testmode true and pluginsupport is false, it should be false
		when(spiedConfig.getSystemProperty("testmode", "false")).thenReturn("true");
		when(spiedConfig.getSystemProperty("pluginsupport", "true")).thenReturn("false");
		assertThat(spiedConfig.isPluginSupported(), is(false));

		// When testmode false and pluginsupport is false, it should be false
		when(spiedConfig.getSystemProperty("testmode", "false")).thenReturn("true");
		when(spiedConfig.getSystemProperty("pluginsupport", "true")).thenReturn("false");
		assertThat(spiedConfig.isPluginSupported(), is(false));

		// When testmode true and pluginsupport is true, it should be false
		when(spiedConfig.getSystemProperty("testmode", "false")).thenReturn("true");
		when(spiedConfig.getSystemProperty("pluginsupport", "true")).thenReturn("true");
		assertThat(spiedConfig.isPluginSupported(), is(true));
	}
}
