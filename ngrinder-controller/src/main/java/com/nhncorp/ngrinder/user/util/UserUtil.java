package com.nhncorp.ngrinder.user.util;

import com.nhncorp.ngrinder.core.model.User;

/**
 * user util
 * 
 * @author Tobi
 * @since
 * @date 2012-6-28
 */
public class UserUtil {

	private static User tmpUser;

	static {
		User user = new User();
		user.setId(987);
		user.setName("tmp_user3");
		setCurrentUser(user);
	}

	// TODO Related functions is not yet complete
	public static User getCurrentUser() {
		return tmpUser;

	}

	public static void setCurrentUser(User user) {
		tmpUser = user;
	}
}
