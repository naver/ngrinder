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
import org.ngrinder.AbstractNGrinderTransactionalTest;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.infra.ArchLoaderInit;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.agent.AgentMonitorServer;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.ngrinder.perftest.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * In addition {@link AbstractNGrinderTransactionalTest}, this class provides basic function to
 * create {@link PerfTest} and run the test.
 *
 * @author JunHo Yoon
 * @since 3.0
 */
public abstract class AbstractAgentReadyTest extends AbstractPerfTestTransactionalTest {
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractAgentReadyTest.class);

	static {
		AgentConfig agentConfig = new AgentConfig.NullAgentConfig(1).init();
		try {
			new ArchLoaderInit().init(agentConfig.getHome().getNativeDirectory());
		} catch (Exception e) {
			e.printStackTrace();
		}
		AgentControllerDaemon agentControllerDaemon = new AgentControllerDaemon(agentConfig);
		agentControllerDaemon.run();

		try {
			Set<String> collector = MonitorConstants.SYSTEM_DATA_COLLECTOR;
			AgentMonitorServer.getInstance().init(MonitorConstants.DEFAULT_MONITOR_PORT, collector, agentConfig);
			AgentMonitorServer.getInstance().start();
		} catch (Exception e) {
			LOG.error("ERROR: {}", e.getMessage());
			LOG.debug("Error while starting Monitor", e);
		}
	}
}
