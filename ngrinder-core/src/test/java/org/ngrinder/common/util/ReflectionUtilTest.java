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
package org.ngrinder.common.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ngrinder.model.User;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since
 */
public class ReflectionUtilTest {

	/**
	 * Test method for
	 * {@link net.grinder.util.ReflectionUtil#getFieldValue(java.lang.Object, java.lang.String)} .
	 */
	@Test
	public void testGetFieldValue() {
		User testUser = new User();
		testUser.setUserId("TMP_UID");
		String rtnUid = (String) ReflectionUtils.getFieldValue(testUser, "userId");
		assertThat(rtnUid, is("TMP_UID"));
	}
}
