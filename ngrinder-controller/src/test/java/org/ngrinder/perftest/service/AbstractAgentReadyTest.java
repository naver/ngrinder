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
package org.ngrinder.perftest.service;

import net.grinder.AgentControllerDaemon;
import org.ngrinder.common.constants.AgentConstants;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.infra.ArchLoaderInit;
import org.ngrinder.monitor.agent.MonitorServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In addition {@link AbstractPerfTestTransactionalTest}, this class provides running agent.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class AbstractAgentReadyTest extends AbstractPerfTestTransactionalTest implements AgentConstants {
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractAgentReadyTest.class);

	static {
		AgentConfig agentConfig = new AgentConfig.NullAgentConfig(1).init();
		try {
			new ArchLoaderInit().init(agentConfig.getHome().getNativeDirectory());
		} catch (Exception e) {
			LOG.error("ArchLoader failed", e);
		}
		AgentControllerDaemon agentControllerDaemon = new AgentControllerDaemon(agentConfig);
		agentControllerDaemon.run();
		try {
			MonitorServer.getInstance().init(agentConfig);
			MonitorServer.getInstance().start();
		} catch (Exception e) {
			LOG.error("Error while starting Monitor", e);
		}
	}
}
