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
package org.ngrinder.perftest.model;

import org.junit.Test;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.model.PerfTest;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

public class PerfTestTest {

	@Test
	public void testGetTargetHostIp() {
		PerfTest test = new PerfTest();
		test.setTargetHosts("aaa.com:1.1.1.1");
		List<String> ipList = test.getTargetHostIP();
		assertThat(ipList.get(0), is("1.1.1.1"));

		test.setTargetHosts(":1.1.1.1");
		ipList = test.getTargetHostIP();
		assertThat(ipList.get(0), is("1.1.1.1"));

		test.setTargetHosts("1.1.1.1");
		ipList = test.getTargetHostIP();
		assertThat(ipList.get(0), is("1.1.1.1"));

		// multiple hosts
		test.setTargetHosts("aaa.com:1.1.1.1,aaabb.com:1.1.1.2");
		ipList = test.getTargetHostIP();
		assertThat(ipList.get(1), is("1.1.1.2"));

		test.setTargetHosts("aaa.com:1.1.1.1,:1.1.1.2");
		ipList = test.getTargetHostIP();
		assertThat(ipList.get(1), is("1.1.1.2"));

		test.setTargetHosts("aaa.com:1.1.1.1,1.1.1.2");
		ipList = test.getTargetHostIP();
		assertThat(ipList.get(1), is("1.1.1.2"));

		test.setTargetHosts("www.test.com:0:0:0:0:0:ffff:3d87:a969,www.test.com:0:0:0:0:0:ffff:a22:4024");
		ipList = test.getTargetHostIP();
		assertThat(ipList.get(1), is("0:0:0:0:0:ffff:a22:4024"));
	}

	@Test
	public void testAddProgressMessage() {
		PerfTest test = new PerfTest();
		test.setProgressMessage("");
		for (int i = 0; i < 1000; i++) {
			test.setLastProgressMessage("HELLO" + i);
		}
		assertThat(test.getProgressMessage().length(), lessThanOrEqualTo(ControllerConstants.MAX_STACKTRACE_STRING_SIZE));
	}

}
