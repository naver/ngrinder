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
package net.grinder.console.manager;

import net.grinder.SingleConsole;
import net.grinder.console.common.ConsoleException;

import org.junit.Test;

public class ConsoleTest {

	@Test
	public void testConsoleCreation() throws ConsoleException, InterruptedException {
		SingleConsole multiConsole1 = new SingleConsole("localhost", 11211);
		multiConsole1.start();
		Thread.sleep(1000);
		multiConsole1.shutdown();
		Thread.sleep(1000);
		SingleConsole multiConsole2 = new SingleConsole("localhost", 11211);
		multiConsole2.start();
		Thread.sleep(1000);
		multiConsole2.shutdown();
	}
}
