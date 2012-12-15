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
package org.ngrinder.monitor.controller;

import java.util.ArrayList;
import java.util.List;

import org.ngrinder.monitor.controller.domain.MonitorAgentInfo;
import org.ngrinder.monitor.controller.domain.MonitorRecorder;
import org.ngrinder.monitor.share.domain.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class description.
 * 
 * @author Tobi
 * @since
 * @date 2012-7-20
 */
public class MonitorRecoderDemo implements MonitorRecorder {

	private static final Logger LOG = LoggerFactory.getLogger(MonitorRecoderDemo.class);

	private boolean running = false;
	private List<String> data = new ArrayList<String>();

	@Override
	public void before() {
		running = true;
	}

	@Override
	public void recoderSystemInfo(String key, SystemInfo systemInfo, MonitorAgentInfo agentInfo) {
		if (running) {
			String record = String.format("Record system info: %s for key:%s", systemInfo, key);
			LOG.debug(record);
			data.add(record);
		}
	}

	@Override
	public void after() {
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public List<String> getData() {
		return data;
	}
}
