package org.ngrinder.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ngrinder.infra.plugin.OnLoginRunnable;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.stereotype.Service;

@Service
public class DefaultLoginPlugin implements OnLoginRunnable {

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private UserService userService;

	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	@Override
	public SecuredUser loadUser(String userId) {
		User userById = userService.getUserById(userId);
		if (userById != null) {
			return new SecuredUser(userById, getClass().getName());
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean authUser(Object encoder, String principle, String encPass, String rawPass, Object salt) {
		if (!((PasswordEncoder) encoder).isPasswordValid(encPass, rawPass, salt)) {
			logger.debug("Authentication failed: password does not match stored value");

			throw new BadCredentialsException(messages.getMessage(
					"AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"), principle);
		}
		return false;
	}

}
