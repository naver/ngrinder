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

/**
 * Plugin extension point for the custom user authentication.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public interface OnLoginRunnable {
	/**
	 * Load user by userId.
	 * 
	 * @param userId
	 *            user id
	 * @return User instance
	 */
	public User loadUser(String userId);

	/**
	 * Validate user by userId and password.
	 * 
	 * Against password can be provided by plugin. In such case encPass, encoder, salt might be
	 * null.
	 * 
	 * @param userId
	 *            user providing id
	 * @param password
	 *            user providing password
	 * @param encPass
	 *            encrypted password
	 * @param encoder
	 *            encoder which encrypts password
	 * @param salt
	 *            salt of encoding
	 * @return true is validated
	 */
	public boolean validateUser(String userId, String password, String encPass, Object encoder, Object salt);

	/**
	 * Save user in plugin.<br/>
	 * This method is only necessary to implement if there is need to save the user in the plugin.
	 * Generally dummy implementation is enough.
	 * 
	 * @param user
	 *            user to be saved.
	 */
	public void saveUser(User user);
}
