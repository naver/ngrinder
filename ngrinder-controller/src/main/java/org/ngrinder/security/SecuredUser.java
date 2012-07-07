package org.ngrinder.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * {@link UserDetails} implementation
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public class SecuredUser implements UserDetails {

	private static final long serialVersionUID = 9160341654874660746L;
	private final String userInfoProviderClass;
	private String authProviderClass;
	private final User user;
	private String timezone;
	private String userLanguage;

	public SecuredUser(User user, String userInfoProviderClass) {
		this.user = user;
		this.userInfoProviderClass = userInfoProviderClass;
		this.authProviderClass = StringUtils.defaultIfEmpty(user.getAuthProviderClass(), userInfoProviderClass);
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>(1);
		roles.add(new SimpleGrantedAuthority(user.getRole().getShortName()));
		return roles;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUserId();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public String getUserInfoProviderClass() {
		return userInfoProviderClass;
	}

	public String getAuthProviderClass() {
		return authProviderClass;
	}

	public User getUser() {
		return user;
	}

	public String getUserLanguage() {
		return userLanguage;
	}

	public void setUserLanguage(String userLanguage) {
		this.userLanguage = userLanguage;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
}
