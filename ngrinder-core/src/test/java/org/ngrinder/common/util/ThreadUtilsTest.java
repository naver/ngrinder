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
package org.ngrinder.common.util;

import org.apache.commons.lang.time.StopWatch;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Thread Util Unit Test.
 *
 * @author Mavlarn
 */
public class ThreadUtilsTest {

	/**
	 * Test method for {@link ThreadUtils#sleep(long)}.
	 */
	@Test
	public void testSleep() {
		StopWatch watch = new StopWatch();
		watch.start();
		ThreadUtils.sleep(1000);
		watch.stop();
		assertThat(watch.getTime()).isGreaterThanOrEqualTo(1000);
		assertThat(watch.getTime()).isLessThan(3000);
	}

	/**
	 * Test method for
	 * {@link ThreadUtils#stopQuietly(java.lang.Thread, java.lang.String)}.
	 */
	@Test
	public void testStopQuietly() {
		Thread newThread = new Thread(new Runnable() {
			@Override
			public void run() {
				int i = 10;
				while (i > 0) {
					ThreadUtils.sleep(200);
				}
			}
		});
		newThread.start();
		ThreadUtils.sleep(500);
		assertThat(newThread.isAlive()).isTrue();
		ThreadUtils.stopQuietly(newThread, "STOPPED!");
		ThreadUtils.sleep(1000);
		assertThat(newThread.isAlive()).isFalse();
	}

}
