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

import com.unboundid.ldap.sdk.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.PropertiesWrapper;
import org.ngrinder.infra.config.Config;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.ngrinder.common.constant.LdapConstants.*;

@Slf4j
@Component
@Profile("production")
@RequiredArgsConstructor
public class NGrinderLdapContext {
	private static final int LDAP_CONNECTION_POOL_SIZE = 4;

	private final Config config;

	private LDAPConnectionPool ldapConnectionPool;

	@PostConstruct
	public void init() {
		config.addSystemConfListener(event -> initialize());
		initialize();
	}

	private void initialize() {
		LDAPConnection ldapConnection = createLdapConnection();
		if (ldapConnection == null) {
			return;
		}

		log.info("LDAP login is enabled");
		try {
			ldapConnectionPool = new LDAPConnectionPool(ldapConnection, LDAP_CONNECTION_POOL_SIZE);
		} catch (LDAPException e) {
			throw new NGrinderRuntimeException(e);
		}
	}

	private LDAPConnection createLdapConnection() {
		if (!isEnabled()) {
			log.info("LDAP login is disabled");
			return null;
		}

		PropertiesWrapper properties = config.getLdapProperties();

		String ldapServer = properties.getProperty(PROP_LDAP_SERVER, "").replace("ldap://", "");
		if (isEmpty(ldapServer)) {
			log.info("LDAP server is not specified. LDAP login is disabled");
			return null;
		}

		int ldapPort = properties.getPropertyInt(PROP_LDAP_PORT);
		String managerDn = properties.getProperty(PROP_LDAP_MANAGER_DN);
		String managerPassword = properties.getProperty(PROP_LDAP_MANAGER_PASSWORD);

		LDAPConnectionOptions ldapConnectionOptions = new LDAPConnectionOptions();
		ldapConnectionOptions.setConnectTimeoutMillis(properties.getPropertyInt(PROP_LDAP_CONNECT_TIMEOUT));
		ldapConnectionOptions.setResponseTimeoutMillis(properties.getPropertyInt(PROP_LDAP_RESPONSE_TIMEOUT));

		try {
			if (isNotEmpty(managerDn) && isNotEmpty(managerPassword)) {
				return new LDAPConnection(ldapConnectionOptions, ldapServer, ldapPort, managerDn, managerPassword);
			} else {
				return new LDAPConnection(ldapConnectionOptions, ldapServer, ldapPort);
			}
		} catch (Exception e) {
			throw new NGrinderRuntimeException(e);
		}
	}

	public LDAPConnection getLdapConnection() {
		try {
			return ldapConnectionPool.getConnection();
		} catch (LDAPException e) {
			throw new NGrinderRuntimeException(e);
		}
	}

	public LDAPConnection getLdapConnection(String userId, String userPassword) {
		try {
			LDAPConnection connection = ldapConnectionPool.getConnection();
			connection.bind(new SimpleBindRequest(userId, userPassword));
			return connection;
		} catch (LDAPException e) {
			throw new NGrinderRuntimeException(e);
		}
	}

	public boolean isEnabled() {
		return config.getLdapProperties().getPropertyBoolean(PROP_LDAP_ENABLED, false);
	}

	public String getUserNameKey() {
		return config.getLdapProperties().getProperty(PROP_LDAP_USER_DISPLAY_NAME);
	}

	public String getUserEmailKey() {
		return config.getLdapProperties().getProperty(PROP_LDAP_USER_EMAIL);
	}

	public String getBaseDN() {
		return config.getLdapProperties().getProperty(PROP_LDAP_BASE_DN, "");
	}

	public String getUserSearchBase() {
		return config.getLdapProperties().getProperty(PROP_LDAP_USER_SEARCH_BASE, "");
	}

	public String getUserFilter() {
		return config.getLdapProperties().getProperty(PROP_LDAP_USER_SEARCH_FILTER);
	}
}
