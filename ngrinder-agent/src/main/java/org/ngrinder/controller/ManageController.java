package org.ngrinder.controller;

import org.ngrinder.service.ManageService;
import org.ngrinder.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/manageController")
public class ManageController {
	private static final Logger LOG = LoggerFactory.getLogger(ManageController.class);

	@Autowired
	private ManageService manageBO;

	/**
	 * Start.
	 */
	@RequestMapping(value = "/start")
	public @ResponseBody
	String start() {
		manageBO.start();
		return JSONUtil.returnSuccess();
	}

	@RequestMapping(value = "/startAlone")
	public @ResponseBody
	String startAlone(@RequestParam String hudsonHost, @RequestParam int hudsonPort) {
		manageBO.startAlone(hudsonHost, hudsonPort);
		return JSONUtil.returnSuccess();
	}

	@RequestMapping(value = "/reStart")
	public @ResponseBody
	String reStart() {
		manageBO.reStart();
		return JSONUtil.returnSuccess();
	}

	/**
	 * Stop.
	 */
	@RequestMapping(value = "/stop")
	public @ResponseBody
	String stop() {
		manageBO.stop();
		return JSONUtil.returnSuccess();
	}

	/**
	 * Checks if is ready.
	 * 
	 * @return true, if is ready
	 */
	@RequestMapping(value = "/isReady")
	public @ResponseBody
	String isReady() {
		boolean result = manageBO.isReady();
		if (result) {
			return JSONUtil.returnSuccess();
		} else {
			return JSONUtil.returnError();
		}
	}

	/**
	 * Load catalina logs.
	 * 
	 * @param hostKey
	 *            Key of agent host
	 * @return messages
	 */
	@RequestMapping(value = "/loadCatalinaLogs")
	public @ResponseBody
	String loadCatalinaLogs(@RequestParam String hostKey) {
		try {
			manageBO.loadCatalinaLogs(hostKey);
			return JSONUtil.returnSuccess();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JSONUtil.returnError(e.getMessage());
		}
	}

	/**
	 * Load grinder logs
	 * 
	 * @param hostKey
	 *            Key of agent host
	 * @param logPath
	 *            Log file path
	 * @return messages @
	 */
	@RequestMapping(value = "/loadGrinderLogs")
	public @ResponseBody
	String loadGrinderLogs(@RequestParam String hostKey, @RequestParam String logPath) {
		try {
			manageBO.loadGrinderLogs(hostKey, logPath);
			return JSONUtil.returnSuccess();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return JSONUtil.returnError(e.getMessage());
		}
	}
}
