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
import org.ngrinder.extension.OnLoginRunnable;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultLdapLoginPlugin implements OnLoginRunnable {
	private final NGrinderLdapContext ldapContext;

	private final UserService userService;

	@Override
	public User loadUser(String userId) {
		if (!ldapContext.isEnabled()) {
			return null;
		}

		Entry userEntry = getUserFromLDAP(userId);
		if (userEntry == null) {
			return null;
		}

		User user = new User();
		user.setUserId(userId);
		user.setUserName(userEntry.getAttribute(ldapContext.getUserNameKey()).getValue());
		user.setEmail(userEntry.getAttribute(ldapContext.getUserEmailKey()).getValue());
		user.setAuthProviderClass(this.getClass().getName());
		user.setEnabled(true);
		user.setExternal(true);
		user.setRole(Role.USER);

		User foundOne = userService.getOne(userId);
		if (foundOne != null) {
			user.setId(foundOne.getId());
			user.setRole(foundOne.getRole());
		}

		return user;
	}

	private Entry getUserFromLDAP(String userId) {
		try {
			String searchBase = normalizeUserSearchBase(ldapContext.getBaseDN(), ldapContext.getUserSearchBase());
			String searchFilter = normalizeUserSearchFilter(ldapContext.getUserFilter(), userId);

			SearchRequest request = new SearchRequest(searchBase, SearchScope.SUB, searchFilter);
			SearchResult result = ldapContext.getLdapConnection().search(request);
			if (result == null) {
				return null;
			}
			if (result.getEntryCount() > 0) {
				return result.getSearchEntries().get(0);
			}
		} catch (LDAPException e) {
			log.error("Cannot find {} in LDAP, ", userId, e);
		}
		return null;
	}

	private String normalizeUserSearchFilter(String userFilter, String userId) {
		if (!userFilter.startsWith("(") || !userFilter.endsWith(")")) {
			userFilter = "(" + userFilter + ")";
		}
		String userIdFilter = String.format("(CN=%s)", userId);

		if (isBlank(userFilter) && isBlank(userId)) {
			return EMPTY;
		}

		if (isBlank(userFilter)) {
			return userIdFilter;
		}

		if (isBlank(userId)) {
			return userFilter;
		}

		return String.format("(&%s%s)", userFilter, userIdFilter);
	}

	private String normalizeUserSearchBase(String baseDN, String userSearchBase) {
		if (isBlank(baseDN) && isBlank(userSearchBase)) {
			return EMPTY;
		}

		if (isBlank(baseDN)) {
			return userSearchBase;
		}

		if (isBlank(userSearchBase)) {
			return baseDN;
		}

		return userSearchBase.trim() + "," + baseDN.trim();
	}

	@Override
	public boolean validateUser(String userId, String password, String encPass, Object encoder, Object salt) {
		if (!ldapContext.isEnabled()) {
			throw new BadCredentialsException("LDAP login is disabled");
		}

		Entry userEntry = getUserFromLDAP(userId);
		try {
			LDAPConnection authConnection = ldapContext.getLdapConnection(userEntry.getDN(), password);
			return authConnection.isConnected();
		} catch (Exception e) {
			throw new BadCredentialsException("Invalid LDAP credential", e);
		}
	}

	@Deprecated
	@Override
	public void saveUser(User user) {
		// do nothing
	}
}
