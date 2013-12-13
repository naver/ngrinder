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
package org.ngrinder.model;

/**
 * Role of the User.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public enum Role {
	/**
	 * General user role who can create performance test entry.
	 */
	USER("U", "General User") {
	},
	/**
	 * Admin user role who can monitors tests.
	 */
	ADMIN("A", "Administrator") {

		/**
		 * Has admin permission or not.
		 * 
		 * @param type	permission type
		 * 
		 * @return has the permission or not
		 */
		public boolean hasPermission(Permission type) {
			switch (type) {
			case GET_ALL_TESTS:
				return true;
			case DELETE_TEST_OF_OTHER:
				return true;
			case CHECK_SCRIPT_OF_OTHER:
				return true;
			case VALIDATE_SCRIPT_OF_OTHER:
				return true;
			case STOP_TEST_OF_OTHER:
				return true;
			case SWITCH_TO_ANYONE:
				return true;
			default:
				return false;
			}
		}
	},
	/**
	 * Super user role who can set system settings and manage user account.
	 */
	SUPER_USER("S", "Super User") {

		/**
		 * Has super permission or not.
		 * 
		 * @param type	permission type
		 * @return has the permission or not
		 */
		public boolean hasPermission(Permission type) {
			switch (type) {
			case GET_ALL_TESTS:
				return true;
			case CHECK_SCRIPT_OF_OTHER:
				return true;
			case VALIDATE_SCRIPT_OF_OTHER:
				return true;
			case SWITCH_TO_ANYONE:
				return true;
			default:
				return false;
			}
		}
	},
	/**
	 * System user role. This is for the automatic batch.
	 */
	SYSTEM_USER("SYSTEM", "System User") {

	};

	private final String shortName;

	private final String fullName;

	/**
	 * Constructor.
	 * 
	 * @param shortName	short name of role... usually 1 sing char
	 * @param fullName	full name of role
	 */
	Role(String shortName, String fullName) {
		this.shortName = shortName;
		this.fullName = fullName;
	}

	/**
	 * Get the short name.
	 * 
	 * @return short name
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * Get full name.
	 * 
	 * @return full name
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * check this role whether has permission.
	 * 
	 * @param type permission type
	 * @return true if can
	 */
	public boolean hasPermission(Permission type) {
		return false;
	}
}
