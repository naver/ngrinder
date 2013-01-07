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

import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;

/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class TagTest {
	
	@Test
	public void testEquals() {
		Tag tag1 = new Tag();
		tag1.setTagValue("testTag1");
		assertTrue(!tag1.equals(new User()));

		Tag tag2 = new Tag();
		tag2.setTagValue("testTag2");
		assertTrue(!tag1.equals(tag2));

		tag2.setTagValue("testTag1");
		tag2.setPerfTests(new HashSet<PerfTest>());
		assertTrue(tag1.equals(tag2));
	}

}
