package org.ngrinder.script.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.ngrinder.common.controller.NGrinderBaseController;
import org.ngrinder.common.util.FileUtil;
import org.ngrinder.common.util.JSONUtil;
import org.ngrinder.script.model.Library;
import org.ngrinder.script.model.Script;
import org.ngrinder.script.service.LibraryService;
import org.ngrinder.script.service.ScriptService;
import org.ngrinder.script.util.LibraryUtil;
import org.ngrinder.script.util.ScriptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping("/script")
public class ScriptController extends NGrinderBaseController {

	// private static final Logger LOG =
	// LoggerFactory.getLogger(ScriptController.class);

	@Autowired
	private ScriptService scriptService;
	
	@Autowired
	private LibraryService libraryService;
	
	@RequestMapping("/list")
	public String getAllScripts(ModelMap model,
			@RequestParam(required = false) String keywords, 
			@RequestParam(required = false) boolean isOwner) { //"fileName"
		
		Page<Script> scripts = scriptService.getScripts(!isOwner, keywords, null);
		List<Library> libraries = libraryService.getLibraries();
		
		model.addAttribute("scripts", scripts);
		model.addAttribute("libraries", libraries);
		model.addAttribute("keywords", keywords);
		model.addAttribute("isOwner", isOwner);
		
		return "script/scriptList";
	}

	@RequestMapping("/detail")
	public String getScript(ModelMap model, Script script, 
			@RequestParam(required = false) Long id,
			@RequestParam(required = false) String historyFileName) {
		if (null == id) {
			scriptService.saveScript(script);
			model.addAttribute("result", script);
		} else {
			Script obj = null;
			if (null == historyFileName || "0".equals(historyFileName)) {
				obj = scriptService.getScript(id);
			} else {
				model.addAttribute("historyFileName", historyFileName);
				obj = scriptService.getScript(id, historyFileName);
			}
			model.addAttribute("result", obj);
		}
		
		return "script/scriptEditor";
	}

	@RequestMapping("/historyContent")
	public String getScriptHistoryContent(@RequestParam long id, @RequestParam String historyName) {
		Script script = scriptService.getScript(id, historyName);
		System.out.println(script);
		return null;
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String createScript(ModelMap model, Script script) {
		scriptService.saveScript(script);
		
		return getScript(model, script, script.getId(), null);
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public String uploadFiles(ModelMap model, Script script, 
			@RequestParam("uploadFile") List<MultipartFile> scriptFiles) {
		for (MultipartFile file : scriptFiles) {
			if (file.getName().toLowerCase().endsWith(".py")) {
				script.setFileSize(file.getSize());
				try {
					script.setContentBytes(file.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				scriptService.saveScript(script);
			} else {
				Library library = new Library();
				library.setFileName(file.getName());
				library.setFileSize(file.getSize());
				try {
					library.setContentBytes(file.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				libraryService.saveLibrary(library);
			}
		}
		
		return getAllScripts(model, "", false);
	}

	@RequestMapping(value = "/deleteScript")
	public String deleteScript(ModelMap model, @RequestParam String ids) {
		String[] idArr = ids.split(",");
		long id = 0;
		for (String idStr : idArr) {
			id = Long.parseLong(idStr);
			scriptService.deleteScript(id);
		}
		
		return getAllScripts(model, "", false);
	}
	
	@RequestMapping(value = "/downloadScript")
	public String downloadScript(HttpServletResponse response, 
			@RequestParam long id, @RequestParam String fileName) {
		boolean success =
			FileUtil.downloadFile(response, ScriptUtil.getScriptFilePath(id, fileName));
		if (!success) {
			response.setContentType("text/html;charset=utf-8");
			PrintWriter writer = null;
			try {
				writer = response.getWriter();
				writer.write("<script type=\"text/javascript\">alert('Download script error.')</script>");
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (null != writer) {
					writer.close();
				}
			}
		}
		
		return null;
	}

	@RequestMapping(value = "/deleteResource")
	public String deleteResource(ModelMap model, @RequestParam String fileName) {
		libraryService.deleteLibrary(fileName);
		
		return getAllScripts(model, "", false);
	}

	@RequestMapping(value = "/downloadResource")
	public String downloadResource(HttpServletResponse response, @RequestParam String fileName) {
		boolean success =
			FileUtil.downloadFile(response, LibraryUtil.getLibFilePath(fileName));
		if (!success) {
			response.setContentType("text/html;charset=utf-8");
			PrintWriter writer = null;
			try {
				writer = response.getWriter();
				writer.write("<script type=\"text/javascript\">alert('Download library error.')</script>");
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (null != writer) {
					writer.close();
				}
			}
		}
		
		return null;
	}
	
	@RequestMapping(value = "/autoSave")
	public @ResponseBody String autoSaveScript(@RequestParam long id, @RequestParam String content) {
		scriptService.autoSave(id, content);
		
		return JSONUtil.returnSuccess();
	}
}
