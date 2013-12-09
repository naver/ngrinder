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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.operation.cotroller.SystemConfigController;
import org.ngrinder.operation.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

public class SystemConfigControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private SystemConfigController controller;

	@Autowired
	private SystemConfigService service;

	@Autowired
	private Config config;

	@Test
	public void testGetSystemConfiguration() {
		Model model = new ExtendedModelMap();
		controller.getOne(model);
		assertThat(model.containsAttribute("content"), is(true));
	}

	@Test
	public void testSaveSystemConfiguration() {
		String oriContent = service.getOne();
		Model model = new ExtendedModelMap();
		String content = "test=My test.";
		try {
			controller.save(model, content);
			ThreadUtils.sleep(2500); //sleep a while to wait for the file monitor to update the system properties.

			assertThat(model.containsAttribute("success"), is(true));
			assertThat(service.getOne(), is(content));
			assertThat(config.getSystemProperties().getProperty("test", ""), is("My test."));
		} finally {
			//reset system config
			controller.save(model, oriContent);
		}
	}
}
