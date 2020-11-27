package org.ngrinder.script.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.script.model.FileEntry;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;

import static org.ngrinder.common.util.CollectionUtils.newArrayList;
import static org.ngrinder.common.util.ExceptionUtils.processException;

/**
 * ScriptHanderFactory which returns appropriate hander for the given {@link FileEntry}.
 *
 * @since 3.2
 */
@Component
@RequiredArgsConstructor
public class ScriptHandlerFactory {

	private final List<ScriptHandler> scriptHandlers;

	@Getter
	private List<ScriptHandler> visibleHandlers;

	/**
	 * Initialize the {@link ScriptHandler}s.
	 */
	@PostConstruct
	public void init() {
		// Sort by the order of scriptHandlers..
		scriptHandlers.sort(Comparator.comparing(ScriptHandler::order));

		// Sort by the order of scriptHandlers..

		visibleHandlers = newArrayList();
		for (ScriptHandler each : this.scriptHandlers) {
			if (each.isCreatable()) {
				visibleHandlers.add(each);
			}
		}
		visibleHandlers.sort(Comparator.comparing(ScriptHandler::displayOrder));

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
