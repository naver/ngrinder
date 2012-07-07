/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
