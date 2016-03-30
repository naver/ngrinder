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
package org.ngrinder.extension;

import org.ngrinder.model.PerfTest;
import org.ngrinder.service.IPerfTestService;

import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * Plugin extension point for {@link PerfTest} start and finish.
 * 
 * This plugin is necessary if you want to be notified whenever the test start and end.
 * 
 * @author JunHo Yoon
 * @since 3.0
 */
public interface OnTestLifeCycleRunnable extends ExtensionPoint {

	/**
	 * Callback method which will be invoked whenever {@link PerfTest} is started.
	 * 
	 * @param perfTest			Performance Test
	 * @param perfTestService	perfTestService interface
	 * @param version			ngrinder version
	 */
	public void start(PerfTest perfTest, IPerfTestService perfTestService, String version);

	/**
	 * Callback method which will be invoked whenever {@link PerfTest} is finished.
	 * 
	 * 
	 * @param perfTest			Performance Test
	 * @param stopReason		stop reason
	 * @param perfTestService	perfTestService interface
	 * @param version			ngrinder version
	 */
	public void finish(PerfTest perfTest, String stopReason, IPerfTestService perfTestService, String version);
}
