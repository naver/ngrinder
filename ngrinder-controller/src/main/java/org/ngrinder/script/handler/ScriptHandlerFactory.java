package org.ngrinder.script.handler;

import static org.ngrinder.common.util.CollectionUtils.newArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.ngrinder.common.exception.NGrinderRuntimeException;
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

	/**
	 * Initialize the {@link ScriptHandler}s.
	 */
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
	 * Get the all handlers except NullScriptHandler.
	 * 
	 * @return all handlers but NullScriptHandler
	 */
	public List<ScriptHandler> getVisibleHandlers() {
		List<ScriptHandler> handlers = newArrayList();
		for (ScriptHandler each : this.scriptHandlers) {
			if (!(each instanceof NullScriptHandler)) {
				handlers.add(each);
			}
		}
		return handlers;
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
		// Actually nothing is reach here.
		throw new NGrinderRuntimeException("no matching handler for " + fileEntry.getPath());
	}

}
