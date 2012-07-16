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

import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.util.JSONUtil;
import org.ngrinder.model.User;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.script.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/perftest")
public class PerfTestController extends NGrinderBaseController {

	// private static final Logger LOG =
	// LoggerFactory.getLogger(ScriptController.class);

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private ScriptService scriptService;

//	private static final int DEFAULT_TEST_PAGE_ZISE = 15;

	@RequestMapping("/list")
	public String getTestList(User user, ModelMap model, @RequestParam(required = false) boolean isFinished,
			@RequestParam(required = false) PageRequest pageable) {

		//not to paging on server side for now. Get all tests and paging/sorting in page.
//		if (pageable == null) {
//			pageable = new PageRequest(0, DEFAULT_TEST_PAGE_ZISE);
//		}
		Page<PerfTest> testList = perfTestService.getTestList(user, isFinished, pageable);
		model.addAttribute("testListPage", testList);
		return "perftest/list";
	}

	@RequestMapping("/detail")
	public String getTestDetail(ModelMap model, @RequestParam(required = false)  Long id) {
		PerfTest test = null;
		if (id != null) {
			test = perfTestService.getPerfTest(id);
		}
		model.addAttribute("test", test);
		model.addAttribute("scriptList", scriptService.getScripts(true, null, null).getContent());
		return "perftest/detail";
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String saveTest(User user, ModelMap model, PerfTest test) {
		perfTestService.savePerfTest(test);

		return getTestList(user, model, false, null);
	}

	@RequestMapping(value = "/clone", method = RequestMethod.POST)
	public String cloneTest(ModelMap model, PerfTest test) {
		test.setId(null);
		perfTestService.savePerfTest(test);

		return "perftest/list";
	}

	@RequestMapping(value = "/deleteTest")
	public @ResponseBody String deleteTest(ModelMap model, @RequestParam Long id) {
		perfTestService.deletePerfTest(id);
		return JSONUtil.returnSuccess();
	}

	@RequestMapping(value = "/deleteTests")
	public @ResponseBody String deleteTests(ModelMap model, @RequestParam String ids) {
		String[] idList = StringUtils.split(ids, ",");
		for (String idStr : idList) {
			long id = Long.valueOf(idStr);
			perfTestService.deletePerfTest(id);
		}
		return JSONUtil.returnSuccess();
	}

	@RequestMapping(value = "/report")
	public String getReport(ModelMap model, @RequestParam long testId) {
		return "perftest/report";
	}
}
