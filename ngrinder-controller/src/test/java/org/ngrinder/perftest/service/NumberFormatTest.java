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
package org.ngrinder.perftest.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class NumberFormatTest {

	@Test
	public void testNumberFormat() {
		PerfTestService perfTestService = new PerfTestService();
		Map<String, String> map = new HashMap<String, String>();
		map.put("HELLO", "100,000");
		assertThat(perfTestService.parseDoubleWithSafety(map, "HELLO", 3d), is(100000D));
	}
}
