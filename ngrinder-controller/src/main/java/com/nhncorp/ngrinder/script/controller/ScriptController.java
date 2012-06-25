package com.nhncorp.ngrinder.script.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nhncorp.ngrinder.core.controller.NGrinderBaseController;
import com.nhncorp.ngrinder.script.model.Library;
import com.nhncorp.ngrinder.script.model.Script;
import com.nhncorp.ngrinder.script.service.ScriptService;

@Controller
@RequestMapping("/script")
public class ScriptController extends NGrinderBaseController {

	// private static final Logger LOG =
	// LoggerFactory.getLogger(ScriptController.class);

	@Autowired
	private ScriptService scriptService;

	@RequestMapping("/list")
	public String getAllScripts() {
		Page<Script> scripts = scriptService.getScripts(null, null);
		System.out.println(scripts);
		return null;
	}

	@RequestMapping("/detail")
	public String getScript(@RequestParam long id) {
		Script script = scriptService.getScript(id);
		System.out.println(script);
		return null;
	}

	@RequestMapping("/historyContent")
	public String getScriptHistoryContent(@RequestParam long id, @RequestParam String historyName) {
		Script script = scriptService.getScript(id, historyName);
		System.out.println(script);
		return null;
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String createScript(@RequestParam Script script) {
		scriptService.saveScript(script);
		return null;
	}

	@RequestMapping(value = "/uploadLibrary", method = RequestMethod.POST)
	public String uploadLibrary(@RequestParam long scriptId, @RequestParam Library library) {
		scriptService.saveLibrary(scriptId, library);
		return null;
	}

	@RequestMapping(value = "/delete")
	public String deleteScript(@RequestParam long id) {
		scriptService.deleteScript(id);
		return null;
	}

}
