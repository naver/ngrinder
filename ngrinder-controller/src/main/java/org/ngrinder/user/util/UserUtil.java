package org.ngrinder.user.util;

import org.ngrinder.user.model.NGrinderUser;

/**
 * user util
 * 
 * @author Tobi
 * @since
 * @date 2012-6-28
 */
// TODO Related functions is not yet complete
public class UserUtil {

	private static NGrinderUser tmpUser;

	static {
		NGrinderUser user = new NGrinderUser();
		user.setId(987);
		user.setName("default_tmp_user");
		setCurrentUser(user);
	}

	public static NGrinderUser getCurrentUser() {
		return tmpUser;

	}

	public static void setCurrentUser(NGrinderUser user) {
		tmpUser = user;
	}
}
