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
package org.ngrinder.agent.service;

import org.ngrinder.infra.config.Config;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Dynamic creation of {@link AgentManagerService} depending on the cluster enable or disable.
 * 
 * @author JunHo Yoon
 * @since 3.1
 */
@Configuration
@EnableScheduling
@EnableTransactionManagement
public class AgentManagerServiceConfig implements ApplicationContextAware {

	@Autowired
	private Config config;

	private ApplicationContext applicationContext;

	/**
	 * Create agent manager service depending on cluster mode.
	 * 
	 * @return {@link AgentManagerService}
	 */
	@Bean(name = "agentManagerService")
	public AgentManagerService agentManagerService() {
		AgentManagerService createBean = 
				(AgentManagerService) applicationContext.getAutowireCapableBeanFactory().autowire(
						config.isCluster() ? ClusteredAgentManagerService.class 
								: AgentManagerService.class, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
		return createBean;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
