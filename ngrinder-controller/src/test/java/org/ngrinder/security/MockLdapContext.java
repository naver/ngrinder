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

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.infra.config.Config;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("unit-test")
@Component
public class MockLdapContext extends NGrinderLdapContext {
	public static final String LDAP_TEST_BASE_DN = "dc=example,dc=com";
	public static final String LDAP_TEST_USER_ID = "ldap-admin";
	public static final String LDAP_TEST_USER_PASSWORD = "admin";

	private InMemoryDirectoryServer server;

	private boolean enabled = false;

	public MockLdapContext(Config config) throws Exception {
		super(config);

		server = new InMemoryDirectoryServer(LDAP_TEST_BASE_DN);
		server.addEntries(
			"dn: dc=example,dc=com",
			"objectClass: top",
			"objectClass: domain",
			"dc: example",
			"",
			"dn: cn=" + LDAP_TEST_USER_ID + ",dc=example,dc=com",
			"objectClass: top",
			"objectClass: person",
			"objectClass: organizationalPerson",
			"sn: LDAP administrator",
			"cn: " + LDAP_TEST_USER_ID,
			"description: LDAP administrator for unit test",
			"userPassword: " + LDAP_TEST_USER_PASSWORD
		);
		server.startListening();
	}

	@Override
	public LDAPConnection getLdapConnection() {
		try {
			return server.getConnection();
		} catch (LDAPException e) {
			throw new NGrinderRuntimeException(e);
		}
	}

	@Override
	public LDAPConnection getLdapConnection(String userId, String userPassword) {
		try {
			LDAPConnection connection = server.getConnection();
			connection.bind(userId, userPassword);
			return connection;
		} catch (LDAPException e) {
			throw new NGrinderRuntimeException(e);
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String getBaseDN() {
		return LDAP_TEST_BASE_DN;
	}
}
