package com.nhncorp.ngrinder.script.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.nhncorp.ngrinder.script.model.Library;
import com.nhncorp.ngrinder.script.model.Script;

public interface ScriptService {

	List<Script> getScripts(String searchStr, Pageable pageable);

	Script getScript(long id);

	Script getScript(long id, String historyName);

	void saveScript(Script script);

	void saveLibrary(long scriptId, Library Library);

	void deleteScript(long id);

	List<String> getHistoryFileName(long id);

	void autoSave(long id, String content);

}
