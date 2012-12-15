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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Test;

public class PerfTestVUserCalcTest {

	@Test
	public void testPerfTestScript() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
		engine.put("A", 10);
		engine.put("B", 4);
		System.out.println(engine.eval(" [(A / 2), (A + 3)];"));
	}

}
