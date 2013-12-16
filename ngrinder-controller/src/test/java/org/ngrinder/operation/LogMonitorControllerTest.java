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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.operation.cotroller.LogMonitorController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class LogMonitorControllerTest extends AbstractNGrinderTransactionalTest {
	public static final Logger LOGGER = LoggerFactory.getLogger(LogMonitorControllerTest.class);
	@Autowired
	private LogMonitorController logMonitorController;

	@Autowired
	private Config config;

	@Test
	public void testLogMonitorController() {
		sleep(3000);
		logMonitorController.enableVerbose(false);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < 100; i++) {
			buffer.append("====================================");
		}
		CoreLogger.LOGGER.info(buffer.toString());
		CoreLogger.LOGGER.info("Core Logger");
		LOGGER.debug("TEST TEST");
		sleep(1000);
		// if logMonitorController.enableVerbose(false), it will check system setting.
		boolean isDebug = config.getControllerProperties().getPropertyBoolean(PROP_CONTROLLER_VERBOSE);
		if (!isDebug) {
			assertThat(getLastMessage(), not(containsString("TEST TEST")));
		}
		CoreLogger.LOGGER.info("Core Logger");
		sleep(4000);
		// assertThat(getLastMessage(), containsString("Core Logger"));

		logMonitorController.enableVerbose(true);
		LOGGER.debug("TEST TEST");
		sleep(1000);
		// assertThat(getLastMessage(), containsString("TEST TEST"));

	}

	private String getLastMessage() {
		HttpEntity<String> lastLog = logMonitorController.getLast();
		JsonParser parser = new JsonParser();
		JsonElement parse = parser.parse(lastLog.getBody());
		String message = parse.getAsJsonObject().get("log").getAsString();
		return message;
	}
}
