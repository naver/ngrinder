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

import junit.framework.Assert;
import net.grinder.SingleConsole;
import net.grinder.console.common.ConsoleException;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.console.model.SampleModelImplementation;
import net.grinder.util.ConsolePropertiesFactory;

import org.junit.Test;

public class PoolableConsoleTest {

	@Test
	public void testPoolableConsole() throws ConsoleException, InterruptedException {
		ConsoleProperties consoleProperties = ConsolePropertiesFactory.createEmptyConsoleProperties();
		consoleProperties.setConsolePort(1111);
		SingleConsole multiConsole1 = new SingleConsole("localhost", 1111);
		multiConsole1.start();
		Thread.sleep(1000);
		SampleModelImplementation component1 = multiConsole1.getConsoleComponent(SampleModelImplementation.class);
		multiConsole1.shutdown();
		Thread.sleep(1000);
		multiConsole1.start();
		SampleModelImplementation component2 = multiConsole1.getConsoleComponent(SampleModelImplementation.class);
		Thread.sleep(1000);
		Assert.assertTrue(component1 == component2);

	}
}
