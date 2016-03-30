/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.extension;

import org.ngrinder.model.User;

import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * Plugin extension point for the custom user authentication.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public interface OnLoginRunnable extends ExtensionPoint {
	/**
	 * Load user by userId.
	 * 
	 * When the user having the given userId exists in the local DB, this method is not called by the nGrinder user
	 * management system. However there are no user in local DB, nGrinder user management calls this to get to know who
	 * it is. If you have LDAP or other system which can returns user id, email, cellphone number, please create the
	 * {@link User} instance in this method using these info so that nGrinder save the user account into DB
	 * automatically after {@link #validateUser(String, String, String, Object, Object)} is passed.
	 * 
	 * @param userId	user id
	 * @return User instance
	 */
	public User loadUser(String userId);

	/**
	 * Validate user with userId and password.
	 * 
	 * encPass / encoder / salt are only when the password is saved in the local DB. When you implement this with remote
	 * password validation system, you may only need userId and password which is input by an user in the nGrinder login page.
	 * 
	 * @param userId	user providing id
	 * @param password	user providing password
	 * @param encPass	encrypted password stored in the DB
	 * @param encoder	encoder which encrypts password in the DB.
	 * @param salt		salt of encoding
	 * @return true is validated
	 */
	public boolean validateUser(String userId, String password, String encPass, Object encoder, Object salt);

	/**
	 * Save the given user. Usually dummy implementation is enough
	 * 
	 * @param user	user to be saved.
	 * @deprecated
	 */
	public void saveUser(User user);
}
