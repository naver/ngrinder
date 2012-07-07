package org.ngrinder.security;

import org.ngrinder.infra.plugin.OnLoginRunnable;

public class MockLoginPlugin implements OnLoginRunnable {

	@Override
	public SecuredUser loadUser(String userId) {
		return null;
	}

	@Override
	public boolean authUser(Object encoder, String principle, String encPass, String rawPass, Object salt) {
		return true;
	}

}
