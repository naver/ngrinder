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
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.config.Config;
import org.ngrinder.operation.cotroller.AnnouncementApiController;
import org.ngrinder.operation.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;

public class AnnouncementControllerTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private AnnouncementApiController controller;

	@Autowired
	private AnnouncementService service;

	@Autowired
	private Config config;

	@Test
	public void testSaveAnnouncement() {
		String content = "My test.";
		controller.save(content);

		assertThat(service.getOne(), is(content));
		assertThat(config.getAnnouncement(), is(content));
	}
}
