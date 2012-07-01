package org.ngrinder.script.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ngrinder.script.model.Script;


/**
 * scripts cache
 * 
 * @author Liu Zhifei
 * @date 2012-6-14
 */
public final class ScriptsCache {

	private ScriptsCache() {
	}

	private static ScriptsCache INSTANCE = new ScriptsCache();

	public static ScriptsCache getInstance() {
		return INSTANCE;
	}

	// / CopyOnWriteArraySet / ConcurrentHashMap
	private final Map<Long, Script> scriptsMap = new HashMap<Long, Script>();

	private final Set<Script> scriptSet = new HashSet<Script>();

	// private final Map<String, Set<Script>> userScriptsMap = new
	// HashMap<String, Set<Script>>();
	// private final Set<Script> sharedScriptSet = new HashSet<Script>();

	public void put(Script script) {
		scriptsMap.put(script.getId(), script);
		scriptSet.add(script);
	}

	public Script get(long id) {
		return scriptsMap.get(id);
	}

	public List<Script> get() {
		return new ArrayList<Script>(scriptSet);
	}

	public void remove(long id) {
		scriptSet.remove(scriptsMap.remove(id));
	}
}
