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
 * Role Permission of the User.
 */
public enum Permission {

	/** Can check all perTest. */
	GET_ALL_TESTS,

	/** Can switch to anyone. */
	SWITCH_TO_ANYONE,

	/** An modify perTest that other created. */
	MODIFY_TEST_OF_OTHER,

	/** Can delete perTest that other created. */
	DELETE_TEST_OF_OTHER,

	/** Can stop perTest that other created. */
	STOP_TEST_OF_OTHER,

	/** Aheck script that other created. */
	CHECK_SCRIPT_OF_OTHER,

	/** Validate script that other created. */
	VALIDATE_SCRIPT_OF_OTHER;

}
