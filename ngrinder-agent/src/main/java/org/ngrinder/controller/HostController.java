package org.ngrinder.controller;

import org.ngrinder.service.HostsService;
import org.ngrinder.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * HostController
 * 
 * @author Liu Zhifei
 * 
 *         2012-3-31
 */
@Controller
@RequestMapping("/hostController")
public class HostController {
	private static final Logger LOG = LoggerFactory.getLogger(HostController.class);

	@Autowired
	private HostsService hostsBO;

	/**
	 * update hosts file content, first back up
	 * 
	 * @param hostsContent
	 */
	@RequestMapping(value = "/updateHostsFile")
	public @ResponseBody
	String updateHostsFile(@RequestParam String hostsContent) {
		try {
			hostsBO.updateHostsFile(hostsContent);
			return JSONUtil.returnSuccess();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JSONUtil.returnError(e.getMessage());
		}
	}

	/**
	 * recover the hosts file from backup
	 * 
	 */
	@RequestMapping(value = "/recoverHostsFile")
	public @ResponseBody
	String recoverHostsFile() {
		try {
			hostsBO.recoverHostsFile();
			return JSONUtil.returnSuccess();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JSONUtil.returnError(e.getMessage());
		}
	}

	/**
	 * get Agent Server time
	 * 
	 */
	@RequestMapping(value = "/getAgentDate")
	public @ResponseBody
	String getAgentDate() {
		return hostsBO.getAgentDate();
	}
}
