/*
 * Copyright (c) 2012-present NAVER Corp.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at https://naver.github.io/ngrinder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.grinder.scriptengine.groovy;

import net.grinder.common.GrinderProperties;
import net.grinder.engine.common.EngineException;
import net.grinder.engine.common.ScriptLocation;
import net.grinder.engine.process.JavaDCRInstrumenterEx;
import net.grinder.scriptengine.DCRContext;
import net.grinder.scriptengine.Instrumenter;
import net.grinder.scriptengine.ScriptEngineService;
import net.grinder.util.FileExtensionMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Groovy script engine service.
 * 
 * @author Mavlarn
 * @since 3.0
 */
public class GroovyScriptEngineService implements ScriptEngineService {

	private final FileExtensionMatcher m_groovyFileMatcher = new FileExtensionMatcher(".groovy");

	@SuppressWarnings("unused")
	private final boolean m_forceDCRInstrumentation;
	private final DCRContext m_dcrContext;

	/**
	 * Constructor.
	 * 
	 * @param properties		Properties.
	 * @param dcrContext		DCR context.
	 * @param scriptLocation	Script location.
	 */
	public GroovyScriptEngineService(GrinderProperties properties, //
					DCRContext dcrContext, ScriptLocation scriptLocation) {

		// This property name is poor, since it really means "If DCR
		// instrumentation is available, avoid the traditional Jython
		// instrumenter". I'm not renaming it, since I expect it only to last
		// a few releases, until DCR becomes the default.
		m_forceDCRInstrumentation = properties.getBoolean("grinder.dcrinstrumentation", false)
		// Hack: force DCR instrumentation for non-Jython scripts.
						|| m_groovyFileMatcher.accept(scriptLocation.getFile());

		m_dcrContext = dcrContext;
	}

	/**
	 * Constructor used when DCR is unavailable.
	 */
	public GroovyScriptEngineService() {
		m_dcrContext = null;
		m_forceDCRInstrumentation = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Instrumenter> createInstrumenters() throws EngineException {

		final List<Instrumenter> instrumenters = new ArrayList<Instrumenter>();

		/*
		 * if (!m_forceDCRInstrumentation) {
		 * System.out.println("m_forceDCRInstrumentation is false."); // must using Instrumentation
		 * }
		 */
		if (m_dcrContext != null) {
			if (instrumenters.size() == 0) {
				instrumenters.add(new JavaDCRInstrumenterEx(m_dcrContext));
			}
		}

		return instrumenters;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ScriptEngine createScriptEngine(ScriptLocation script) throws EngineException {

		if (m_groovyFileMatcher.accept(script.getFile())) {
			return new GroovyScriptEngine(script);
		}

		return null;
	}

}
