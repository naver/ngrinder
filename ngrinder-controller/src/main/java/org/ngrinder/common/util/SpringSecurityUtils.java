package org.ngrinder.common.util;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

/**
 * Spring Security Utility functions.
 *
 * @since 3.4.2
 */
public class SpringSecurityUtils {

	/**
	 * Get current user authorities.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<? extends GrantedAuthority> getCurrentAuthorities() {
		return (getContext().getAuthentication() != null) ? getContext().getAuthentication().getAuthorities() : Collections.EMPTY_LIST;
	}

	/**
	 * Check current user has specific authority.
	 */
	public static boolean containsAuthority(Collection<? extends GrantedAuthority> authorities, String requiredAuth) {
		for (GrantedAuthority grantedAuthority : authorities) {
			if (grantedAuthority.getAuthority().equals(requiredAuth)) {
				return true;
			}
		}
		return false;
	}
}
