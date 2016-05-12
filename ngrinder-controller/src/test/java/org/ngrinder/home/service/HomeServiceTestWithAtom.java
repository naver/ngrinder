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

import org.junit.Test;
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.home.model.PanelEntry;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class HomeServiceTestWithAtom {

	private HomeService homeService = new HomeService();

	@Test
	public void testAtom() {
		List<PanelEntry> leftPanelEntries = homeService.getLeftPanelEntries("https://github.com/naver/ngrinder/wiki.atom");

		assertThat(leftPanelEntries.size(), greaterThan(2));
		assertThat(leftPanelEntries.size(), lessThanOrEqualTo(8));
		for (PanelEntry panelEntry : leftPanelEntries) {
			assertThat(panelEntry.getTitle(), notNullValue());
			assertThat(panelEntry.getLink(), startsWith("http"));
		}
	}
}
