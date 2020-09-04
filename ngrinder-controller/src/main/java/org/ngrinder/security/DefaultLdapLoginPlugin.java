package org.ngrinder.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.extension.OnLoginRunnable;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.springframework.stereotype.Component;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultLdapLoginPlugin implements OnLoginRunnable {
	private final NGrinderLdapContext ldapContext;

	@Override
	public User loadUser(String userId) {
		Attributes userAttributes = getUserFromLDAP(userId);
		if (userAttributes == null) {
			return null;
		}

		try {
			User user = new User();
			user.setUserId(userId);
			user.setUserName((String) userAttributes.get(ldapContext.getUserNameKey()).get());
			user.setEmail((String) userAttributes.get(ldapContext.getUserEmailKey()).get());
			user.setAuthProviderClass(this.getClass().getName());
			user.setEnabled(true);
			user.setExternal(true);
			user.setRole(Role.USER);
			return user;
		} catch (NamingException e) {
			log.error("Fail to load user by LDAP login plugin", e);
			throw new NGrinderRuntimeException(e);
		}
	}

	private Attributes getUserFromLDAP(String userId) {
		SearchResult searchResult = searchUser(userId);
		if (searchResult == null) {
			return null;
		}
		return searchResult.getAttributes();
	}

	private SearchResult searchUser(String userId) {
		SearchResult searchResult = null;
		try {

			String searchBase = normalizeUserSearchBase(ldapContext.getBaseDN(), ldapContext.getUserSearchBase());
			NamingEnumeration<SearchResult> enumeration = ldapContext.getLdapContext().search(searchBase, ldapContext.getUserFilter(), ldapContext.getSearchControls());
			while (enumeration.hasMore()) {
				SearchResult result = enumeration.next();
				String commonName = (String) result.getAttributes().get("CN").get();

				if (userId.equalsIgnoreCase(commonName)) {
					searchResult = result;
					break;
				}
			}
		} catch (NamingException e) {
			log.error("Cannot find {} in LDAP, ", userId, e);
		}
		return searchResult;
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
		// TODO: validate user with LDAP
		return false;
	}

	@Deprecated
	@Override
	public void saveUser(User user) {
		// do nothing
	}
}
