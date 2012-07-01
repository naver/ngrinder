package org.ngrinder.user.util;

import org.ngrinder.user.model.User;

/**
 * user util
 * 
 * @author Tobi
 * @since
 * @date 2012-6-28
 */
// TODO Related functions is not yet complete
public class UserUtil {

	private static User tmpUser;

	static {
		User user = new User();
		user.setId(987);
		user.setName("default_tmp_user");
		setCurrentUser(user);
	}

	public static User getCurrentUser() {
		return tmpUser;

	}

	public static void setCurrentUser(User user) {

		tmpUser = user;
	}
}
