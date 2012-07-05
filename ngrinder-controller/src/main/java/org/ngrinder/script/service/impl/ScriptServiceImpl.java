package org.ngrinder.script.service.impl;

import java.util.List;

import org.ngrinder.script.model.Script;
import org.ngrinder.script.repository.ScriptDao;
import org.ngrinder.script.service.ScriptService;
import org.ngrinder.script.util.ScriptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Script service implement
 * 
 * @author Liu Zhifei
 * @date 2012-6-13
 */
@Service
public class ScriptServiceImpl implements ScriptService {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(ScriptServiceImpl.class);

	@Autowired
	private ScriptDao scriptDao;

	@Override
	public Page<Script> getScripts(boolean share, String searchStr, Pageable pageable) {
		Page<Script> scripts = scriptDao.getScripts(share, searchStr, pageable);
		return scripts;
	}

	@Override
	public Script getScript(long id) {
		Script script = scriptDao.findOne(id);
		if (null != script) {
			ScriptUtil.getContent(script);
			script.setCacheContent(ScriptUtil.getScriptCache(id));
			script.setHistoryFileNames(this.getHistoryFileName(id));
		}
		return script;
	}

	@Override
	public Script getScript(long id, String historyName) {
		Script script = this.getScript(id);
		if (null != script) {
			ScriptUtil.getHistoryContent(script, historyName);
		}
		return script;
	}

	@Override
	public void saveScript(Script script) {
		if (null != script.getId() && 0 != script.getId().longValue()) {
			Script scriptOld = this.getScript(script.getId());
			if (null != scriptOld) {
				ScriptUtil.saveScriptHistoryFile(scriptOld);
			}
		}
		scriptDao.save(script);
		ScriptUtil.saveScriptFile(script);
		ScriptUtil.deleteScriptCache(script.getId());
	}

	@Override
	public void deleteScript(long id) {
		scriptDao.delete(id);
		ScriptUtil.deleteScriptFile(id);
	}

	private List<String> getHistoryFileName(long id) {
		List<String> historyFileNames = ScriptUtil.getHistoryFileNames(id);
		return historyFileNames;
	}

	@Override
	public void autoSave(long id, String content) {
		ScriptUtil.saveScriptCache(id, content);
	}

}
