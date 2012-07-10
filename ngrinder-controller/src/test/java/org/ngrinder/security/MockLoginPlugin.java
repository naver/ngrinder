package org.ngrinder.security;

import org.ngrinder.infra.plugin.OnLoginRunnable;
import org.ngrinder.model.User;

public class MockLoginPlugin implements OnLoginRunnable {

	@Override
	public User loadUser(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validateUser(String userId, String password, String encPass, Object encoder, Object salt) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void saveUser(User user) {
		// TODO Auto-generated method stub

	}


}
