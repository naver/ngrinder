/*
 * Copyright (c) 2012-present NAVER Corp.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at https://naver.github.io/ngrinder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ngrinder.security;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertTrue;
import static org.ngrinder.security.MockLdapContext.LDAP_TEST_USER_ID;
import static org.ngrinder.security.MockLdapContext.LDAP_TEST_USER_PASSWORD;

public class DefaultLdapLoginPluginTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private DefaultLdapLoginPlugin defaultLdapLoginPlugin;

	@Autowired
	private MockLdapContext ldapContext;

	@Before
	public void setup() {
		ldapContext.setEnabled(true);
	}

	@After
	public void tearDown() {
		ldapContext.setEnabled(false);
	}

	@Test
	public void testValidateUser() {
		assertTrue(defaultLdapLoginPlugin.validateUser(LDAP_TEST_USER_ID, LDAP_TEST_USER_PASSWORD, null, null, null));
	}
}
