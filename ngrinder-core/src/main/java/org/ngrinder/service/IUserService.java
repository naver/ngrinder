package org.ngrinder.service;

import org.ngrinder.model.Role;
import org.ngrinder.model.User;

public interface IUserService {

	public abstract void encodePassword(User user);

	/**
	 * create user.
	 * 
	 * @param user
	 *            include id, userID, fullName, role, password.
	 * 
	 * @return result
	 */
	public abstract User saveUser(User user);

	/**
	 * Add normal user
	 * 
	 * @param user
	 */
	public abstract void saveUser(User user, Role role);

}