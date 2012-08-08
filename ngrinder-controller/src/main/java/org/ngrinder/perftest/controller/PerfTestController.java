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
package org.ngrinder.perftest.controller;

import static org.ngrinder.common.util.Preconditions.checkNotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.exception.NGrinderRuntimeException;
import org.ngrinder.common.util.FileDownloadUtil;
import org.ngrinder.common.util.JSONUtil;
import org.ngrinder.model.User;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.model.ProcessAndThread;
import org.ngrinder.perftest.model.Status;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.PageableDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Performance Test Controller.
 * 
 * @author Mavlarn
 * @author JunHo Yoon
 */
@Controller
@RequestMapping("/perftest")
public class PerfTestController extends NGrinderBaseController {

	private static final Logger LOG = LoggerFactory.getLogger(PerfTestController.class);

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private FileEntryService fileEntiryService;

	// private static final int DEFAULT_TEST_PAGE_ZISE = 15;

	/**
	 * Get Performance test lists.
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            modelMap
	 * @param isFinished
	 *            only list finished project
	 * @param pageable
	 *            page
	 * @return perftest/list
	 */
	@RequestMapping("/list")
	public String getTestList(User user, @RequestParam(required = false) String query,
			@RequestParam(required = false) boolean onlyFinished,
			@PageableDefaults(pageNumber = 0, value = 10) Pageable pageable, ModelMap model) {
		// FIXME
		// not to paging on server side for now. Get all tests and
		// paging/sorting in page.
		// if (pageable == null) {
		// pageable = new PageRequest(0, DEFAULT_TEST_PAGE_ZISE);
		// }
		Page<PerfTest> testList = perfTestService.getPerfTestList(user, query, onlyFinished, pageable);
		model.addAttribute("testListPage", testList);
		model.addAttribute("onlyFinished", onlyFinished);
		model.addAttribute("query", query);
		model.addAttribute("page", pageable);
		if (pageable != null && pageable.getSort() != null && pageable.getSort().iterator().hasNext()) {
			Order sortProp = pageable.getSort().iterator().next();
			model.addAttribute("sortColumn", sortProp.getProperty());
			model.addAttribute("sortDirection", sortProp.getDirection());
		}
		return "perftest/list";
	}

	/**
	 * Get performance test detail on give perf test id
	 * 
	 * @param user
	 *            user
	 * @param model
	 *            model
	 * @param id
	 *            performance test id
	 * @return "perftest/detail"
	 */
	@RequestMapping("/detail")
	public String getTestDetail(User user, @RequestParam(required = false) Long id, ModelMap model) {
		PerfTest test = null;
		if (id != null) {
			test = perfTestService.getPerfTest(id);
			if (test == null || !user.equals(test.getCreatedUser())) {
				throw new NGrinderRuntimeException("PerfTest " + id + " is not allowed to show for user " + user);
			}
		}
		
		model.addAttribute(PARAM_TEST, test);
		List<FileEntry> scriptList = null;
		try {
			scriptList = fileEntiryService.getAllFileEntries(user);
		} catch (NGrinderRuntimeException e) {
			LOG.error("Cannot get script list of user:", e);
		}
		model.addAttribute(PARAM_SCRIPT_LIST, scriptList);
		return "perftest/detail";
	}

	/**
	 * Create a new test or clone a current test.
	 * 
	 * @param user
	 * @param model
	 * @param test
	 * @return
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String saveTest(User user, ModelMap model, PerfTest test) {
		// Test can only be cloned, but not allowed to modified, so set id as
		// null,
		// to make sure it will create a new test.
		test.setId(null);
		perfTestService.savePerfTest(test);
		return "redirect:/perftest/list";
	}

	/**
	 * Calculate vuser assignment policy based on request vuser number.
	 * 
	 * @param model
	 * @param newVuser
	 *            how many vusers whil be used.
	 * @return JSON
	 */
	@RequestMapping(value = "/updateVuser")
	public @ResponseBody
	String updateVuser(@RequestParam int newVuser, ModelMap model) {
		ProcessAndThread processAndThread = perfTestService.calcProcessAndThread(newVuser);
		Map<String, Object> rtnMap = new HashMap<String, Object>(3);
		rtnMap.put(JSON_SUCCESS, true);
		rtnMap.put(PARAM_THREAD_COUNT, processAndThread.getThreadCount());
		rtnMap.put(PARAM_PROCESS_COUNT, processAndThread.getProcessCount());
		return JSONUtil.toJson(rtnMap);
	}

	@RequestMapping(value = "/deleteTests")
	public @ResponseBody
	String deleteTests(ModelMap model, @RequestParam String ids) {
		String[] idList = StringUtils.split(ids, ",");
		for (String idStr : idList) {
			try {
				perfTestService.deletePerfTest(Long.valueOf(idStr));
			} catch (NumberFormatException e) {
				LOG.error("Can't delete a test (id=" + idStr + ") : {}", e);
			}
		}
		return JSONUtil.returnSuccess();
	}

	@RequestMapping(value = "/report")
	public String getReport(ModelMap model, @RequestParam long testId) {
		PerfTest test = perfTestService.getPerfTest(testId);
		model.addAttribute("test", test);
		return "perftest/report";
	}

	@RequestMapping(value = "/getReportData")
	public @ResponseBody
	String getReportData(ModelMap model, @RequestParam long testId, @RequestParam(required = true) String dataType,
			@RequestParam int imgWidth) {
		List<Object> reportData = null;
		String[] dataTypes = StringUtils.split(dataType, ",");
		Map<String, Object> rtnMap = new HashMap<String, Object>(1 + dataTypes.length);
		rtnMap.put(JSON_SUCCESS, true);
		for (String dt : dataTypes) {
			try {
				reportData = perfTestService.getReportData(testId, dt, imgWidth);

				rtnMap.put(dt, reportData);
			} catch (Exception e) {
				// just skip if one report data doesn't exist.
				LOG.error("Get report data failed. type: " + dt, e);
			}
		}

		return JSONUtil.toJson(rtnMap);
	}

	@RequestMapping(value = "/downloadReportData")
	public void downloadReportData(HttpServletResponse response, @RequestParam long testId) {
		File targetFile = perfTestService.getReportFile(testId);
		FileDownloadUtil.downloadFile(response, targetFile);
	}

	@RequestMapping(value = "/running/refresh")
	public String refreshTestRunning(ModelMap model, @RequestParam long testId) {
		PerfTest test = perfTestService.getPerfTest(testId);
		checkNotNull(test);
		Map<String, Object> result = null;
		if (test.getStatus() == Status.TESTING) {
			result = perfTestService.getStatistics(test.getPort());
			model.addAttribute(PARAM_RESULT_SUB, result);
		}
		return "perftest/refreshContent";
	}
}
