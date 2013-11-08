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
package org.ngrinder.perftest.service.monitor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.ngrinder.monitor.share.domain.SystemInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



/**
 * Class description.
 *
 * @author Mavlarn
 * @since
 */
public class MonitorClientSerivceTest {
	
	@Test
	public void test() throws IOException {
		File tempRepo = new File(System.getProperty("java.io.tmpdir"), "test-repo");
		tempRepo.mkdir();
		tempRepo.deleteOnExit();
		MonitorClientService client = new MonitorClientService();
		client.init("127.0.0.1", 13243, tempRepo, null);
		
		Map<String, SystemDataModel> rtnMap = new HashMap<String, SystemDataModel>();
		SystemInfo info1 = client.getMonitorData();
		if (info1 == null) {
			return;
		}
		info1.setCustomValues("111,222");
		SystemDataModel data1 = new SystemDataModel(info1, "3.1.2");
		rtnMap.put("test1", data1);
		
		SystemInfo info2 = client.getMonitorData();
		SystemDataModel data2 = new SystemDataModel(info2, "3.1.2");
		rtnMap.put("test2", data2);
		
		Gson gson = new Gson();
		String jsonStr = gson.toJson(rtnMap);
		System.out.println(jsonStr);
		
		@SuppressWarnings("unchecked")
		Map<String, SystemDataModel> back = gson.fromJson(jsonStr, HashMap.class);
		System.out.println(back);

		System.out.println("**********************************");
		Gson gson3 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		String jsonStr3 = gson3.toJson(rtnMap);
		System.out.println(jsonStr3);
		@SuppressWarnings("unchecked")
		Map<String, SystemDataModel> back3 = gson3.fromJson(jsonStr3, HashMap.class);
		System.out.println(back3);
	}

}
