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
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import java.io.IOException;

public class ConsoleEntryTest {

	@Test
	public void testEqualsObject() throws IOException {
		ConsoleEntry console1 = new ConsoleEntry("127.0.0.1", 12221);
		ConsoleEntry console2 = new ConsoleEntry("127.0.0.1", 12221);
		assertThat(console1, is(console2));
		assertThat(console1.hashCode(), is(console2.hashCode()));
		console1.occupySocket();
		console1.releaseSocket();
		console1.occupySocket();
		console2.setPort(222);
		assertThat(console1, not(console2));
		assertThat(console1.hashCode(), not(console2.hashCode()));

		console2.setPort(null);
		assertThat(console1, not(console2));
		assertThat(console1.hashCode(), not(console2.hashCode()));
	}

}
