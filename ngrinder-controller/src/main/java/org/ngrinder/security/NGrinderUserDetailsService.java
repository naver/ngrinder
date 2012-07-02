package org.ngrinder.security;

import org.ngrinder.user.model.SecuredUser;
import org.ngrinder.user.model.User;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("ngrinderUserDetailsService")
public class NGrinderUserDetailsService implements UserDetailsService {

	@Autowired
	private UserService userService;

	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException, DataAccessException {
		User user = userService.getUserById(userId);
		if (user != null) {
			return new SecuredUser(user);
		} else {
			throw new UsernameNotFoundException(userId + " is not found.");
		}
	}
}
