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

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.Matchers.is;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * CollectionUtils unit test
 *
 * @author Mavlarn
 */
public class CollectionUtilsTest {

	/**
	 * Test method for {@link org.ngrinder.common.util.CollectionUtils#selectSome(java.util.Set, int)}.
	 */
	@Test
	public void testSelectSome() {
		Set<Integer> intSet = new HashSet<Integer>();
		intSet.add(1);
		intSet.add(2);
		intSet.add(3);
		intSet.add(4);
		intSet.add(5);
		intSet.add(6);
		Set<Integer> rtnSet = CollectionUtils.selectSome(intSet, 3);
		assertThat(rtnSet.size(), is(3));
		assertTrue(rtnSet.contains(1));
		assertTrue(rtnSet.contains(2));
		assertTrue(rtnSet.contains(3));
	}

}
