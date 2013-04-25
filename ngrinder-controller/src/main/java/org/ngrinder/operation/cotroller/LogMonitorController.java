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
package org.ngrinder.operation.cotroller;

import static org.ngrinder.common.util.CollectionUtils.buildMap;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.springframework.http.HttpEntity;
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
 * This is only available in the non-clustered instance.
 * 
 * @author JunHo Yoon
 * 
 */
@Controller
@RequestMapping("/operation/log")
@PreAuthorize("hasAnyRole('A')")
public class LogMonitorController extends NGrinderBaseController {

	private static final int LOGGER_BUFFER_SIZE = 10000;

	/**
	 * Latest log.
	 */
	private volatile StringBuffer stringBuffer = new StringBuffer(LOGGER_BUFFER_SIZE);

	private Tailer tailer;

	private long count = 0;
	private long modification = 0;

	/**
	 * Initialize the {@link Tailer}.
	 */
	@PostConstruct
	public void init() {
		if (!clustered()) {
			initTailer();
		}
	}

	/**
	 * Initialize tailer.
	 */
	private synchronized void initTailer() {
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
		String logFileName = "ngrinder.log";

		return new File(getConfig().getHome().getGlobalLogFile(), logFileName);
	}

	/**
	 * Finalize bean.
	 */
	@PreDestroy
	public void destroy() {
		if (!clustered()) {
			tailer.stop();
		}
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
		model.addAttribute("verbose", getConfig().isVerbose());
		return "operation/logger";
	}

	/**
	 * Get the last log.
	 * 
	 * @return log json
	 */
	@RequestMapping("/last")
	public HttpEntity<String> getLastLog() {
		return toJsonHttpEntity(buildMap("index", count, "modification", modification, "log", stringBuffer));
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
		getConfig().initLogger(verbose);
		initTailer();
		return toJsonHttpEntity(buildMap("success", true));
	}

}
