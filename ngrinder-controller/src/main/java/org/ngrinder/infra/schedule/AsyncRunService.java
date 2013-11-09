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
package org.ngrinder.infra.schedule;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Wrapper for all async-call.
 * 
 * This is used for pre-fetching sth.
 *
 * @author JunHo Yoon
 * @since 3.3
 */
@Component
public class AsyncRunService {
	/**
	 * Run async job.
	 * @param runnable job to run
	 */
	@Async
	public void runAsync(Runnable runnable) {
		runnable.run();
	}
}
