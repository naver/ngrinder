package org.ngrinder.script.service;

import org.ngrinder.script.model.Script;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ScriptService {

	Page<Script> getScripts(boolean share, String searchStr, Pageable pageable);

	Script getScript(long id);

	Script getScript(long id, String historyName);

	void saveScript(Script script);

	void deleteScript(long id);

	void autoSave(long id, String content);

}
