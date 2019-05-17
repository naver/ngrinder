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

import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.model.AgentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;

/**
 * @since 3.5.0
 */
@RestController
@RequestMapping("/agent/api")
@PreAuthorize("hasAnyRole('A', 'S')")
public class AgentManagerApiController extends BaseController {

	@Autowired
	private AgentManagerService agentManagerService;

	/**
	 * Get the current all agents state.
	 *
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A', 'S', 'U')")
	@GetMapping("/states/")
	public HttpEntity<String> getStates() {
		List<AgentInfo> agents = agentManagerService.getAllVisible();
		return toJsonHttpEntity(getAgentStatus(agents));
	}

	private List<Map<String, Object>> getAgentStatus(List<AgentInfo> agents) {
		List<Map<String, Object>> statuses = newArrayList(agents.size());
		for (AgentInfo each : agents) {
			Map<String, Object> result = newHashMap();
			result.put("id", each.getId());
			result.put("port", each.getPort());
			result.put("icon", each.getState().getCategory().getIconName());
			result.put("state", each.getState());
			statuses.add(result);
		}
		return statuses;
	}
}
