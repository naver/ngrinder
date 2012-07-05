/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.ngrinder.agent.controller;

import org.ngrinder.agent.model.Agent;
import org.ngrinder.agent.service.AgentService;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * agent controller
 * 
 * @author Tobi
 * @since
 * @date 2012-7-5
 */
@Controller
@RequestMapping("/agent")
public class AgentController extends NGrinderBaseController {

	// private static final Logger LOG =
	// LoggerFactory.getLogger(AgentController.class);

	@Autowired
	private AgentService agentService;

	@RequestMapping("/list")
	public String getAgents(ModelMap model, @RequestParam(required = false) String keywords) {

		Page<Agent> agents = agentService.getAgents(keywords, null);

		model.addAttribute("agents", agents);
		model.addAttribute("keywords", keywords);

		return "agent/agentList";
	}

	@RequestMapping("/detail")
	public String getAgent(ModelMap model, @RequestParam(required = false) Long id) {

		Agent agent = agentService.getAgent(id);

		model.addAttribute("result", agent);
		return "agent/agentDetail";
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String createAgent(ModelMap model, Agent agent) {
		agentService.saveAgent(agent);

		return getAgent(model, agent.getId());
	}

	@RequestMapping(value = "/deleteScript")
	public String deleteAgent(ModelMap model, @RequestParam String ids) {
		String[] idArr = ids.split(",");
		long id = 0;
		for (String idStr : idArr) {
			id = Long.parseLong(idStr);
			agentService.deleteAgent(id);
		}

		return getAgents(model, "");
	}

}
