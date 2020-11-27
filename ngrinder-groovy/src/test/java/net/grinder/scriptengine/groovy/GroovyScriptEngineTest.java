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

import net.grinder.engine.common.EngineException;
import net.grinder.engine.common.ScriptLocation;
import net.grinder.engine.process.JUnitThreadContextInitializer;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Class description.
 *
 * @author Mavlarn
 */
public class GroovyScriptEngineTest {

	@Test
	public void testRunGroovyScript() throws EngineException, NoSuchFieldException, IllegalAccessException {

		JUnitThreadContextInitializer init = new JUnitThreadContextInitializer();
		init.initialize();

		// for test, used to get groovy source file.
		String file = getClass().getClassLoader().getResource("org/ngrinder/TestRunner.groovy").getFile();
		GroovyScriptEngine engine = new GroovyScriptEngine(new ScriptLocation(new File(file).getAbsoluteFile()));

		init.attachWorkerThreadContext();
		GroovyScriptEngine.GroovyWorkerRunnable worker = (GroovyScriptEngine.GroovyWorkerRunnable) engine
				.createWorkerRunnable();
		assertStaticField(engine.m_groovyClass, "callCount1", 0);
		assertStaticField(engine.m_groovyClass, "callCount2", 0);
		worker.run();
		assertStaticField(engine.m_groovyClass, "callCount1", 1);
		assertStaticField(engine.m_groovyClass, "callCount2", 1);
		worker.run();
		assertStaticField(engine.m_groovyClass, "callCount1", 2);
		assertStaticField(engine.m_groovyClass, "callCount2", 2);

	}

	private void assertStaticField(Class clazz, String fieldName, Object expectedValue) throws IllegalAccessException,
			NoSuchFieldException {
		assertThat(clazz.getDeclaredField(fieldName).get(null), is(expectedValue));
	}
}
