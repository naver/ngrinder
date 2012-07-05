package org.ngrinder.perftest.controller;

import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.perftest.model.PerfTest;
import org.ngrinder.perftest.service.TestService;
import org.ngrinder.script.model.Script;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/perftest")
public class PerfTestController extends NGrinderBaseController {

	// private static final Logger LOG =
	// LoggerFactory.getLogger(ScriptController.class);

	@Autowired
	private TestService testService;
	
	@RequestMapping("/list")
	public String getTestList(ModelMap model,
			@RequestParam(required = false) String keywords, 
			@RequestParam(required = false) boolean isFinished,
			@RequestParam(required = true) PageRequest pageable) {
		
		String currUserId = getCurrentUserInfo(P_USERID);
		testService.getTestList(currUserId, isFinished, pageable);
		
		return "perftest/list";
	}

	@RequestMapping("/detail")
	public String getScript(ModelMap model, Script script, 
			@RequestParam(required = false) Long id,
			@RequestParam(required = false) String historyFileName) {

		
		return "perftest/detail";
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String createTestt(ModelMap model, PerfTest test) {
		testService.savePerfTest(test);

		return "perftest/list";
	}


	@RequestMapping(value = "/delete")
	public String deleteTestt(ModelMap model, @RequestParam String ids) {

		return "perftest/list";
	}
	
}
