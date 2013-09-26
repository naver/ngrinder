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

package org.ngrinder.infra.config;

import org.apache.commons.io.FileUtils;
import org.ngrinder.common.model.Home;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

/**
 * Application life-cycle event listener.
 * 
 * This class is used to clean up the several locks.
 * 
 * @author JunHo Yoon
 * @since 3.1
 */
@Service
public class ApplicationListenerBean implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	private Config config;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context
	 * .ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		Home exHome = config.getExHome();
		if (exHome.exists()) {
			FileUtils.deleteQuietly(exHome.getSubFile("shutdown.lock"));
			FileUtils.deleteQuietly(exHome.getSubFile("no_more_test.lock"));
		}
	}
}