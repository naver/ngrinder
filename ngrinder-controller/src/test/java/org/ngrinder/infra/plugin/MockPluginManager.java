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
package org.ngrinder.infra.plugin;

import org.ngrinder.infra.config.Config;
import org.pf4j.DefaultPluginManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Profile("unit-test")
@Component
public class MockPluginManager extends PluginManager {

	public MockPluginManager(Config config, DefaultPluginManager manager) {
		super(config, manager);
	}

	@PostConstruct
	public void init() {

	}

	@Override
	protected boolean isPluginSupportEnabled() {
		return true;
	}

}
