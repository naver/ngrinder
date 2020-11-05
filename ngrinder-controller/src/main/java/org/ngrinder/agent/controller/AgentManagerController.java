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

import org.ngrinder.common.controller.annotation.GlobalControllerModel;
import org.ngrinder.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Agent management controller.
 *
 * @since 3.1
 */
@SuppressWarnings("unused")
@Controller
@RequestMapping("/agent")
@GlobalControllerModel
@PreAuthorize("hasAnyRole('A', 'S')")
public class AgentManagerController {

	/**
	 * Agents list
	 */
	@GetMapping({"", "/", "/list"})
	@PreAuthorize("hasAnyRole('A', 'S', 'U')")
	public String getAll(final User user) {
		return "app";
	}

	/**
	 * Get the agent detail info for the given agent ip and name.
	 *
	 * @param user  current user
	 * @param ip    agent ip
	 * @param name  agent name
	 * @return agent/agentDetail
	 */
	@GetMapping("/{ip}/{name}")
	public String getOne(final User user, @PathVariable String ip, @PathVariable String name) {
		return "app";
	}

}
