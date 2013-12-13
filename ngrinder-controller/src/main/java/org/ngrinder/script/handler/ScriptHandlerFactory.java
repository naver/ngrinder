package org.ngrinder.script.handler;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.script.model.FileEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.ExceptionUtils.processException;

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

	private List<ScriptHandler> visibleHandlers;

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

		// Sort by the order of scriptHandlers..

		visibleHandlers = newArrayList();
		for (ScriptHandler each : this.scriptHandlers) {
			if (!(each instanceof NullScriptHandler)) {
				visibleHandlers.add(each);
			}
		}
		Collections.sort(visibleHandlers, new Comparator<ScriptHandler>() {
			@Override
			public int compare(ScriptHandler o1, ScriptHandler o2) {
				return o1.displayOrder().compareTo(o2.displayOrder());
			}
		});

	}

	/**
	 * Get the all handlers except NullScriptHandler.
	 * 
	 * @return all handlers but NullScriptHandler
	 */
	public List<ScriptHandler> getVisibleHandlers() {
		return visibleHandlers;
	}

	/**
	 * Get the appropriate {@link ScriptHandler} for the given fileEntry.
	 * 
	 * @param fileEntry	fileEntry to be handled
	 * @return {@link ScriptHandler}. {@link NullScriptHandler} if none is available.
	 */
	public ScriptHandler getHandler(FileEntry fileEntry) {
		for (ScriptHandler handler : scriptHandlers) {
			if (handler.canHandle(fileEntry)) {
				return handler;
			}
		}
		// Actually nothing is reach here.
		throw processException("no matching handler for " + fileEntry.getPath());
	}

	/**
	 * Get the appropriate {@link ScriptHandler} for the given key.
	 * 
	 * @param key	ScriptHandler key
	 * @return {@link ScriptHandler}. {@link NullScriptHandler} if none is available.
	 */
	public ScriptHandler getHandler(String key) {
		for (ScriptHandler handler : scriptHandlers) {
			if (StringUtils.equals(handler.getKey(), key)) {
				return handler;
			}
		}
		// Actually nothing is reach here.
		throw processException("no matching handler for " + key);
	}

}
