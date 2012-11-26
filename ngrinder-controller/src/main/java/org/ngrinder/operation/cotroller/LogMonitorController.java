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
package org.ngrinder.operation.cotroller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.infra.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Log monitor controller.
 * 
 * This class runs with {@link Tailer} implementation. Whenever the underlying log file is changed.
 * this class gets the changes. and keep them(max 10000 byte) in the memory. Whenever user requests
 * the log, it returns latest changes with the index of the log.
 * 
 * @author JunHo Yoon
 * 
 */
@Controller
@RequestMapping("/operation/log")
@PreAuthorize("hasAnyRole('A', 'S')")
public class LogMonitorController extends NGrinderBaseController {

	private static final int LOGGER_BUFFER_SIZE = 10000;

	@Autowired
	private Config config;

	/**
	 * Latest log.
	 */
	private volatile StringBuffer stringBuffer = new StringBuffer(LOGGER_BUFFER_SIZE);

	private HttpHeaders commonResponseHeaders;
	private Tailer tailer;

	private long count = 0;
	private long modification = 0;

	/**
	 * Initialize the {@link Tailer}.
	 */
	@PostConstruct
	public void init() {
		initTailer();
		commonResponseHeaders = new HttpHeaders();
		commonResponseHeaders.set("content-type", "application/json; charset=UTF-8");
		commonResponseHeaders.setPragma("no-cache");
	}

	/**
	 * Initialize tailer.
	 */
	private void initTailer() {
		File logFile = getLogFile();
		if (tailer != null) {
			tailer.stop();
		}
		tailer = Tailer.create(logFile, new TailerListenerAdapter() {
			/**
			 * Handles a line from a Tailer.
			 * 
			 * @param line
			 *            the line.
			 */
			public void handle(String line) {
				synchronized (stringBuffer) {
					if (stringBuffer.length() + line.length() > 5000) {
						count++;
						modification = 0;
						stringBuffer = new StringBuffer();
					}
					modification++;
					if (stringBuffer.length() > 0) {
						stringBuffer.append("<br>");
					}
					stringBuffer.append(line.replace("\n", "<br>"));
				}
			}
		}, 1000, true);
	}

	/**
	 * Get log file.
	 * 
	 * @return log file
	 */
	File getLogFile() {
		String logFileName;
		if (Config.NON_REGION.equals(config.getRegion())) {
			logFileName = "ngrinder.log";
		} else {
			logFileName = "ngrinder_" + config.getRegion() + ".log";
		}
		return new File(config.getHome().getGloablLogFile(), logFileName);
	}

	/**
	 * Finalize bean.
	 */
	@PreDestroy
	public void destroy() {
		tailer.stop();
	}

	/**
	 * Logger first page.
	 * 
	 * @param model
	 *            model
	 * @return "operation/logger"
	 */
	@RequestMapping("")
	public String getLog(Model model) {
		model.addAttribute("verbose", config.isVerbose());
		return "operation/logger";
	}

	/**
	 * Get the last log.
	 * 
	 * @return log json
	 */
	@RequestMapping("/last")
	public HttpEntity<String> getLastLog() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("index", count);
		map.put("modification", modification);
		map.put("log", stringBuffer);
		return new HttpEntity<String>(toJson(map), commonResponseHeaders);
	}

	/**
	 * Turn on verbose log mode.
	 * 
	 * @param verbose
	 *            true if verbose mode
	 * @return success message if successful
	 */
	@RequestMapping("/verbose")
	public HttpEntity<String> enableVerbose(@RequestParam(value = "verbose", defaultValue = "false") Boolean verbose) {
		config.initLogger(verbose);
		initTailer();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("success", true);
		return new HttpEntity<String>(toJson(map), commonResponseHeaders);
	}

	/**
	 * Reload System properties.
	 * 
	 * @return success message if successful
	 */
	@RequestMapping("/refresh")
	public HttpEntity<String> refreshSystemProperties() {
		config.loadSystemProperties();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("success", true);
		return new HttpEntity<String>(toJson(map), commonResponseHeaders);
	}
}
