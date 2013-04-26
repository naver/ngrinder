package org.ngrinder.script.handler;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.ngrinder.script.model.FileEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ScriptHanderFactory which returns appropriate hander for the given {@link FileEntry}.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
@Component
public class ScriptHandlerFactory {

	@Autowired
	private List<ScriptHandler> scriptHandlers;

	@Autowired
	private NullScriptHandler nullScriptHandler;

	@PostConstruct
	public void init() {
		// Sort by the order of scriptHandlers..
		Collections.sort(scriptHandlers, new Comparator<ScriptHandler>() {
			@Override
			public int compare(ScriptHandler o1, ScriptHandler o2) {
				return o1.order().compareTo(o2.order());
			}
		});
	}

	/**
	 * Get the appropriate {@link ScriptHandler} for the given fileEntry.
	 * 
	 * @param fileEntry
	 *            fileEntry to be handled
	 * @return {@link ScriptHandler}. {@link NullScriptHandler} if none is available.
	 */
	public ScriptHandler getHandler(FileEntry fileEntry) {
		for (ScriptHandler handler : scriptHandlers) {
			if (handler.canHandle(fileEntry)) {
				return handler;
			}
		}
		return nullScriptHandler;
	}

}
