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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.agent.model.AgentInfo;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.util.FileDownloadUtil;
import org.ngrinder.common.util.HttpContainerContext;
import org.ngrinder.infra.config.Config;
import org.ngrinder.monitor.controller.model.SystemDataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * agent controller.
 * 
 * @author Tobi
 * @since 3.0
 */
@Controller
@RequestMapping("/agent")
@PreAuthorize("hasAnyRole('A', 'S')")
public class AgentManagerController extends NGrinderBaseController {

	@Autowired
	private AgentManagerService agentManagerService;

	@Autowired
	private Config config;

	@Autowired
	private HttpContainerContext httpContainerContext;

	/**
	 * Get agent list.
	 * 
	 * @param model
	 *            model
	 * @return viewName
	 */
	@RequestMapping({ "", "/", "/list" })
	public String getAgentList(ModelMap model) {
		List<AgentInfo> agents = agentManagerService.getAllActiveAgentInfoFromDB();
		model.addAttribute("agents", agents);

		File directory = config.getHome().getDownloadDirectory();
		final String contextPath = httpContainerContext.getCurrentRequestUrlFromUserRequest();
		final List<String> downloads = new ArrayList<String>();
		directory.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith("ngrinder")) {
					StringBuilder url = new StringBuilder(config.getSystemProperties().getProperty("http.url",
									contextPath));
					url.append("/agent/download/" + name);
					downloads.add(url.toString());
				}
				return true;
			}
		});
		model.addAttribute("downloadLinks", downloads);
		return "agent/agentList";
	}

	/**
	 * approve agent, so that it can accept test to run on.
	 * 
	 * @param id
	 *            id of agent to be approved
	 * @param approve
	 *            approve or not
	 * @return agent/agentList
	 */
	@RequestMapping(value = "approve", method = RequestMethod.POST)
	public String approveAgent(@RequestParam("id") Long id,
					@RequestParam(value = "approve", defaultValue = "true", required = false) boolean approve) {
		agentManagerService.approve(id, approve);
		return "agent/agentList";
	}

	/**
	 * stop agent, so that it can accept test to run on.
	 * 
	 * @param ids
	 *            comma separating id list
	 * @return agent/agentList
	 */
	@RequestMapping(value = "stop", method = RequestMethod.POST)
	@ResponseBody
	public String stopAgent(@RequestParam("ids") String ids) {
		String[] split = StringUtils.split(ids, ",");
		for (String each : split) {
			agentManagerService.stopAgent(Long.parseLong(each));
		}
		return returnSuccess();
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
		AgentInfo agent = agentManagerService.getAgent(id, false);
		model.addAttribute("agent", agent);
		return "agent/agentDetail";
	}

	/**
	 * Download agent.
	 * 
	 * @param fileName
	 *            file path of agent
	 * @param response
	 *            reponse.
	 */
	@RequestMapping(value = "/download/{fileName}")
	public void downloadAgent(@PathVariable String fileName, HttpServletResponse response) {
		File ngrinderFile = new File(config.getHome().getDownloadDirectory(), fileName);
		FileDownloadUtil.downloadFile(response, ngrinderFile);
	}

	/**
	 * Get the current system performance info for given ip.
	 * 
	 * @param model
	 *            model
	 * @param id
	 *            id
	 * @return json message
	 */
	@RequestMapping("/systemDataModel")
	@ResponseBody
	public String getCurrentMonitorData(ModelMap model, @RequestParam Long id, @RequestParam String ip,
					@RequestParam String name) {
		Map<String, Object> returnMap = new HashMap<String, Object>(3);
		agentManagerService.requestShareAgentSystemDataModel(id);
		SystemDataModel systemData = agentManagerService.getAgentSystemDataModel(ip, name);
		systemData = systemData != null ? systemData : new SystemDataModel();
		returnMap.put(JSON_SUCCESS, true);
		returnMap.put("systemData", systemData);
		return toJson(returnMap);
	}

}
