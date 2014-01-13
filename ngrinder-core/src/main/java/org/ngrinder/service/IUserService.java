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
package org.ngrinder.service;

import org.ngrinder.model.User;

/**
 * User service interface. This interface is visible to plugins
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public interface IUserService {

	/**
	 * Encode password of the given user.
	 *
	 * @param user user
	 */
	public abstract void encodePassword(User user);

	/**
	 * Get user by user id.
	 *
	 * @param userId user id
	 * @return user
	 * @since 3.3
	 */
	public abstract User getOne(String userId);


	/**
	 * Save user without password encoding step.
	 *
	 * @param user include id, userID, fullName, role, password.
	 * @return result
	 * @since 3.3
	 */
	public User saveWithoutPasswordEncoding(User user);


	/**
	 * Save user.
	 *
	 * @param user include id, userID, fullName, role, password.
	 * @return result
	 */
	public abstract User save(User user);


	/**
	 * Create user.
	 *
	 * This method exists to avoid ModelAspect injection.
	 *
	 * @param user include id, userID, fullName, role, password.
	 * @return result
	 */
	public abstract User createUser(User user);

}