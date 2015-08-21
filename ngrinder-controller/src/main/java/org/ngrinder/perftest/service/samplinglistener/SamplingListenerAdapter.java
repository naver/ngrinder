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
package org.ngrinder.perftest.service.samplinglistener;

import net.grinder.SingleConsole;
import net.grinder.SingleConsole.SamplingLifeCycleListener;
import net.grinder.console.communication.AgentProcessControlImplementation.AgentStatus;
import net.grinder.statistics.StatisticsSet;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.model.PerfTest;
import org.ngrinder.model.Status;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.PerfTestService;

import java.io.File;

/**
 * Sampling Listener Adapter
 *
 * @author JunHo Yoon
 * @since 3.4
 */
public class SamplingListenerAdapter implements SamplingLifeCycleListener {


    @Override
    public void onSamplingStarted() {
        // Fall through
    }

    @Override
    public void onSampling(File file, StatisticsSet intervalStatistics, StatisticsSet cumulativeStatistics) {
// Fall through
    }

    @Override
    public void onSamplingEnded() {
        // Fall through
    }
}
