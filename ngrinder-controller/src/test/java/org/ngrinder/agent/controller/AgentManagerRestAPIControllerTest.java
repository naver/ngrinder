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
package org.ngrinder.agent.controller;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class AgentManagerRestAPIControllerTest {
	@Ignore
	@Test
	public void testRestAPI() throws IOException {
		HttpClient client = new HttpClient();
		// To be avoided unless in debug mode
		Credentials defaultcreds = new UsernamePasswordCredentials("admin", "111111");
		client.getParams().setAuthenticationPreemptive(true);
		client.getState().setCredentials(AuthScope.ANY, defaultcreds);
		PutMethod method = new PutMethod("http://localhost:8080/agent/api/36");
		final HttpMethodParams params = new HttpMethodParams();
		params.setParameter("action", "approve");
		method.setParams(params);
		final int i = client.executeMethod(method);
		System.out.println(method.getResponseBodyAsString());
	}
}
