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
package net.grinder.scriptengine.kotlin;

import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import net.grinder.engine.common.EngineException;
import net.grinder.engine.common.ScriptLocation;
import net.grinder.script.Grinder;
import net.grinder.scriptengine.ScriptEngineService;
import net.grinder.scriptengine.ScriptExecutionException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileReader;
import java.io.IOException;

public class KotlinScriptEngine implements ScriptEngineService.ScriptEngine {
	private static final String KOTLIN_SCRIPT_EXTENSION = "kts";

	private final ScriptEngineFactory kotlinEngineFactory;

	private final Class<?> kotlinClass;

	public KotlinScriptEngine(ScriptLocation script) throws EngineException {
		this.kotlinEngineFactory = new ScriptEngineManager().getEngineFactories()
			.stream()
			.filter(factory -> factory.getExtensions().contains(KOTLIN_SCRIPT_EXTENSION))
			.findFirst()
			.orElseThrow(() -> new EngineException("Cannot find kotlin script engine"));
		ScriptEngine kotlinEngine = kotlinEngineFactory.getScriptEngine();

		try {
			KClass<?> kClass = (KClass<?>) kotlinEngine.eval(new FileReader(script.getFile()));
			this.kotlinClass = JvmClassMappingKt.getJavaClass(kClass);
		} catch (IOException | ScriptException e) {
			throw new KotlinScriptExecutionException("Cannot execute kotlin script properly", e);
		}
	}

	@Override
	public ScriptEngineService.WorkerRunnable createWorkerRunnable() throws EngineException {
		return new KotlinWorkerRunnable(kotlinClass);
	}

	@Override
	public ScriptEngineService.WorkerRunnable createWorkerRunnable(Object testRunner) throws EngineException {
		return new KotlinWorkerRunnable(kotlinClass);
	}

	@Override
	public void shutdown() throws EngineException {

	}

	@Override
	public String getDescription() {
		return String.format("KotlinScriptEngine running with kotlin version: %s", kotlinEngineFactory.getLanguageVersion());
	}

	public static final class KotlinWorkerRunnable implements ScriptEngineService.WorkerRunnable {

		private static final String TEST_METHOD_NAME = "doTest";

		private final Class<?> kotlinClass;

		public KotlinWorkerRunnable(Class<?> kotlinClass) {
			this.kotlinClass = kotlinClass;
		}

		@Override
		public void run() throws ScriptExecutionException {
			try {
				Object testRunner = kotlinClass.newInstance();
				kotlinClass.getDeclaredMethod(TEST_METHOD_NAME).invoke(testRunner);
			} catch (Exception e) {
				throw new KotlinScriptExecutionException("Fail to run kotlin script", e);
			}
		}

		@Override
		public void shutdown() throws ScriptExecutionException {

		}
	}

	private static class KotlinScriptExecutionException extends ScriptExecutionException {
		public KotlinScriptExecutionException(String s) {
			super(s);
		}

		public KotlinScriptExecutionException(String s, Throwable t) {
			super(s, t);
		}
	}
}
