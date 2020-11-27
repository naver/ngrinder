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
package org.ngrinder.operation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.ngrinder.common.util.CollectionUtils.buildMap;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class ScriptConsoleControllerTest extends AbstractNGrinderTransactionalTest {
	@Autowired
	MockScriptConsoleApiController scriptController;

	@Test
	public void runScriptTest() {
		Map<String, Object> result;

		Map<String, Object> param = buildMap("script", "");
		result = scriptController.run(param);
		assertThat((String) result.get("result"), is(emptyString()));
		param.put("script", "print \'hello\'");
		result = scriptController.run(param);
		assertThat(result.get("result"), notNullValue());
		assertThat((String) result.get("result"), containsString("hello"));

		param.put("script", "int a = 1");
		scriptController.run(param);
		param.put("script", "print a");
		result = scriptController.run(param);
		assertThat((String) result.get("result"), containsString("No such property"));
	}
}
