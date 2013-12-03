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
package net.grinder.util;

import net.grinder.scriptengine.ScriptEngineService;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static net.grinder.util.ClassLoaderUtilities.allResourceLines;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

public class EngineDiscoveryTest {

	@Test
	public void testGroovyScriptEngineInclusion() throws IOException {
		final List<String> implementationNames = allResourceLines(getClass().getClassLoader(),
				ScriptEngineService.RESOURCE_NAME);
		assertThat(implementationNames, hasItem("net.grinder.scriptengine.groovy.GroovyScriptEngineService"));

	}


}
