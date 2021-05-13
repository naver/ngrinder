/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ngrinder.script.handler;

import org.ngrinder.common.util.PathUtils;
import org.ngrinder.script.model.FileEntry;
import org.springframework.stereotype.Component;

import javax.script.Compilable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

/**
 * Kotlin {@link ScriptHandler}.
 *
 * @since 3.2
 */
@Component
public class KotlinScriptHandler extends ScriptHandler {

	/**
	 * Constructor.
	 */
	public KotlinScriptHandler() {
		this("kotlin", "kts", "Kotlin", "text/x-kotlin", true);
	}

	/**
	 * Constructor.
	 *
	 * @param key           key
	 * @param extension     extension
	 * @param title         title
	 * @param codeMirrorKey code mirror key
	 * @param creatable     creatable
	 */
	public KotlinScriptHandler(String key, String extension, String title, String codeMirrorKey, boolean creatable) {
		super(key, extension, title, codeMirrorKey, creatable);
	}

	@Override
	protected Integer order() {
		return 290;
	}

	@Override
	public String checkSyntaxErrors(String path, String script) {
		try {
			ScriptEngine kotlinScriptEngine = new ScriptEngineManager().getEngineByExtension("kts");
			Reader reader = new FileReader(script);
			Compilable compiler = (Compilable) kotlinScriptEngine;
			compiler.compile(reader);
		} catch (ScriptException | FileNotFoundException e) {
			return e.getMessage();
		}
		return null;
	}

	@Override
	public Integer displayOrder() {
		return 290;
	}

	/**
	 * Get the default quick test file.
	 *
	 * @param basePath base path
	 * @return quick test file
	 */
	@Override
	public FileEntry getDefaultQuickTestFilePath(String basePath) {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath(PathUtils.join(basePath, "TestRunner." + getExtension()));
		return fileEntry;
	}

	@Override
	public String getScriptTemplateName() {
		return "basic_template_kotlin.ftl";
	}
}
