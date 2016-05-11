/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at https://naver.github.io/ngrinder
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
