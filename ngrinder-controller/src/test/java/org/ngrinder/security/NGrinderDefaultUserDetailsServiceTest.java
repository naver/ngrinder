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

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.plugin.MockPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

public class NGrinderDefaultUserDetailsServiceTest extends AbstractNGrinderTransactionalTest {
	@Autowired
	public MockPluginManager mockPluginManager;

	@Autowired
	public NGrinderUserDetailsService userDetailsService;

	@Before
	public void before() {
		mockPluginManager.initPluginFramework();
	}

	@Test
	public void testPluginAuth() {
		// if passing a default user, admin,
		SecuredUser loadUserByUsername = (SecuredUser) userDetailsService.loadUserByUsername("admin");
		// Default should be resolved by DefaultLoginPlugins
		assertThat("admin should not be null", loadUserByUsername, notNullValue());
		assertThat("default user(admin) should be retrieved from DefaultLoginPlugin",
						loadUserByUsername.getUserInfoProviderClass(), nullValue());
	}

	@Test
	public void testUnknownUser() {
		// if passing a unknown user,
		UserDetails user = null;
		try {
			user = userDetailsService.loadUserByUsername("unknown22");
		} catch (Exception e) {
			return;
		}
		fail("Exception should occus " + ToStringBuilder.reflectionToString(user));
	}
}
