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
package org.ngrinder.common.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ngrinder.model.User;
import org.ngrinder.perftest.controller.PerfTestController;
import org.ngrinder.perftest.service.AbstractAgentReadyTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class description.
 * 
 * @author Mavlarn
 * @since
 */
public class BaseControllerTest extends AbstractAgentReadyTest {

	// BaseController is not a component, use its sub-class to test.
	@Autowired
	private PerfTestController perfTestController;

	@Test
	public void testCurrentUser() {
		User currUser = perfTestController.currentUser();
		assertThat(currUser, notNullValue());
	}

	@Test
	public void testGetMessages() {
		String errMsg = perfTestController.getMessages("home.qa.message");
		assertThat(errMsg, is("You can ask any question here."));
	}

}
