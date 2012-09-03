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

@Controller
@RequestMapping("/operation/log")
@PreAuthorize("hasAnyRole('A', 'S')")
public class LogMonitorController extends NGrinderBaseController {
	private HttpHeaders responseHeaders;
	@Autowired
	private Config config;

	private volatile StringBuffer stringBuffer = new StringBuffer(10000);

	private Tailer tailer;

	private long count = 0;
	private long modification = 0;

	@PostConstruct
	public void init() {
		File logFile = new File(config.getHome().getGloablLogFile(),
				"ngrinder.log");
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
					stringBuffer.append("<br/>").append(
							line.replace("\n", "<br/>"));
				}
			}
		});

		responseHeaders = new HttpHeaders();
		responseHeaders.set("content-type", "application/json; charset=UTF-8");
	}

	@PreDestroy
	public void destroy() {
		tailer.stop();
	}

	@RequestMapping("")
	public String getLog() {
		return "operation/logger";
	}

	@RequestMapping("/last")
	public HttpEntity<String> getLastLog() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("index", count);
		map.put("modification", modification);
		map.put("log", stringBuffer);
		return new HttpEntity<String>(JSONUtil.toJson(map), responseHeaders);
	}
}
