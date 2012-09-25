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
import org.ngrinder.common.util.JSONUtil;
import org.ngrinder.infra.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Log monitor controller.
 * 
 * This class runs with {@link Tailer} implementation. Whenever the underlying
 * log file is changed. this class gets the changes. and keep them(max 10000
 * byte) in the memory. Whenever user requests the log, it returns latest
 * changes with the index of the log.
 * 
 * @author JunHo Yoon
 * 
 */
@Controller
@RequestMapping("/operation/log")
@PreAuthorize("hasAnyRole('A', 'S')")
public class LogMonitorController extends NGrinderBaseController {
	private HttpHeaders responseHeaders;
	@Autowired
	private Config config;

	/**
	 * Latest log.
	 */
	private volatile StringBuffer stringBuffer = new StringBuffer(10000);

	private Tailer tailer;

	private long count = 0;
	private long modification = 0;

	/**
	 * Initialize the {@link Tailer}.
	 */
	@PostConstruct
	public void init() {
		File logFile = new File(config.getHome().getGloablLogFile(), "ngrinder.log");
		
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
					stringBuffer.append("<br/>").append(line.replace("\n", "<br/>"));
				}
			}
		}, 1000, true);

		responseHeaders = new HttpHeaders();
		responseHeaders.set("content-type", "application/json; charset=UTF-8");
	}

	@PreDestroy
	public void destroy() {
		tailer.stop();
	}

	/**
	 * Logger first page.
	 * 
	 * @return "operation/logger"
	 */
	@RequestMapping("")
	public String getLog() {
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
		return new HttpEntity<String>(JSONUtil.toJson(map), responseHeaders);
	}
}
