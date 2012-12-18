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
package org.ngrinder.home.service;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.home.model.PanelEntry;
import org.springframework.beans.factory.annotation.Autowired;

public class HomeAsynchServiceTest extends AbstractNGrinderTransactionalTest {

	@Autowired
	private HomeService homeService;

	@Autowired
	private HomeAsyncService homeAsynchService;
	
	@Test
	public void testGetEntries() throws IOException {
		homeAsynchService.getLeftPanelEntries();
		List<PanelEntry> leftPanelEntries = homeService.getLeftPanelEntries();
		assertThat(leftPanelEntries.size(), greaterThan(2));
		assertThat(leftPanelEntries.size(), lessThanOrEqualTo(8));

		homeAsynchService.getRightPanelEntries();
		List<PanelEntry> rightPanel = homeService.getRightPanelEntries();
		assertThat(rightPanel.size(), greaterThanOrEqualTo(2));
		assertThat(rightPanel.size(), lessThanOrEqualTo(8));

	}
}
