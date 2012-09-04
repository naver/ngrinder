package org.ngrinder.security;

import org.ngrinder.extension.OnLoginRunnable;
import org.ngrinder.model.User;

public class MockLoginPlugin implements OnLoginRunnable {

	@Override
	public User loadUser(String userId) {
		return null;
	}

	@Override
	public boolean validateUser(String userId, String password, String encPass, Object encoder, Object salt) {
		return false;
	}

	@Override
	public void saveUser(User user) {
		// Do nothing
	}

}
