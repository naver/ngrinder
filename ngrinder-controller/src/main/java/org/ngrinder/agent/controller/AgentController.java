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

import java.util.List;

import org.ngrinder.agent.model.AgentInfo;
import org.ngrinder.agent.service.AgentService;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * agent controller.
 * 
 * @author Tobi
 * @since 3.0
 */
@Controller
@RequestMapping("/agent")
public class AgentController extends NGrinderBaseController {

	@Autowired
	private AgentService agentService;

	/**
	 * Get agenet list.
	 * 
	 * @param model
	 *            model
	 * @param keywords
	 *            search keyword
	 * @return viewName
	 */
	@RequestMapping({ "", "/", "/list" })
	public String getAgents(ModelMap model) {
		List<AgentInfo> agents = agentService.getAgents();
		model.addAttribute("agents", agents);
		return "agent/agentList";
	}

	/**
	 * Get agent detail info.
	 * 
	 * @param model
	 *            model
	 * @param id
	 *            agent id
	 * @return agent/agentDetail
	 */
	@RequestMapping("/detail")
	public String getAgent(ModelMap model, @RequestParam(required = false) Long id) {
		AgentInfo agent = agentService.getAgent(id);
		model.addAttribute("agent", agent);
		return "agent/agentDetail";
	}

	/**
	 * Create agent.
	 * 
	 * @param model
	 *            model
	 * @param agent
	 *            agent model
	 * @return agent/agentDetail
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String createAgent(ModelMap model, AgentInfo agent) {
		return getAgent(model, agent.getId());
	}

	/**
	 * Delete agent.
	 * 
	 * @param model
	 *            model
	 * @param ids
	 *            agent ids
	 * @return agent/agentList
	 */
	@RequestMapping(value = "/delete")
	public String deleteAgent(ModelMap model, @RequestParam String ids) {
		if (ids == null) {
			return getAgents(model);
		}

		String[] idArr = ids.split(",");
		if (idArr != null) {
			for (String idStr : idArr) {
				try {
					agentService.deleteAgent(Long.parseLong(idStr));
				} catch (NumberFormatException ignored) {
				}
			}
		}
		return getAgents(model);
	}
}
