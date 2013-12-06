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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class PathUtilsTest {

	/**
	 * Test method for {@link PathUtils#removePrependedSlash(java.lang.String)}.
	 */
	@Test
	public void testRemovePrependedSlash() {
		String path = "/aaa/bbb/vvv";
		String newPath = PathUtils.removePrependedSlash(path);
		assertTrue(path.contains(newPath));

		String path2 = "//aaa/bbb/vvv";
		newPath = PathUtils.removePrependedSlash(path2);
		assertTrue(newPath.equals(path));
	}

}
