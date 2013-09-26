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

import java.util.Locale;

import org.ngrinder.common.constant.NGrinderConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Wrapper for all aync-calls used in HomeService.
 * 
 * This is used for pre-fetching home news entries.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
@Component
public class HomeAsyncService {
	@Autowired
	private HomeService homeService;

	@Autowired
	private MessageSource messageSource;

	/**
	 * Get the right panel entries in async way.
	 */
	@Async
	public void getRightPanelEntries() {
		String message = messageSource.getMessage(NGrinderConstants.NGRINDER_QNA_RSS_URL_KEY, null, new Locale("en"));
		homeService.getRightPanelEntries(message);
	}

	/**
	 * Get the left panel entries in async way.
	 */
	@Async
	public void getLeftPanelEntries() {
		homeService.getLeftPanelEntries();
	}
}
