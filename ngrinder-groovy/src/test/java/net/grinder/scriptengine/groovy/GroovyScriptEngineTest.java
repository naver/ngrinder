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
package net.grinder.scriptengine.groovy;

import java.io.File;

import net.grinder.engine.common.EngineException;
import net.grinder.engine.common.ScriptLocation;
import net.grinder.engine.process.JUnitThreadContextInitializer;

import org.junit.Test;

/**
 * Class description.
 * 
 * @author Mavlarn
 */
public class GroovyScriptEngineTest {

	@Test
	public void testRunGroovyScript() throws EngineException {
		JUnitThreadContextInitializer init = new JUnitThreadContextInitializer();
		init.initialize();
		// for test, used to get groovy source file.
		File scriptFile = new File("src/test/java/org/ngrinder/TestRunner.groovy");
		GroovyScriptEngine engine = new GroovyScriptEngine(new ScriptLocation(scriptFile));
		init.attachWorkerThreadContext();
		GroovyScriptEngine.GroovyWorkerRunnable worker = (GroovyScriptEngine.GroovyWorkerRunnable) engine
						.createWorkerRunnable();
		worker.run();
	}
}
