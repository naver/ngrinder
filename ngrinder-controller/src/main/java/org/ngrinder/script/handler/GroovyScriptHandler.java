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

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.ngrinder.common.util.PathUtils;
import org.ngrinder.script.model.FileEntry;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;

import static org.ngrinder.common.util.NoOp.noOp;

/**
 * Groovy {@link ScriptHandler}.
 *
 * @author JunHo Yoon
 * @since 3.2
 */
@Component
public class GroovyScriptHandler extends ScriptHandler {

	/**
	 * Constructor.
	 */
	public GroovyScriptHandler() {
		this("groovy", "groovy", "Groovy", "groovy");
	}

	/**
	 * Constructor.
	 *
	 * @param key           key
	 * @param extension     extension
	 * @param title         title
	 * @param codeMirrorKey code mirror key
	 */
	public GroovyScriptHandler(String key, String extension, String title, String codeMirrorKey) {
		super(key, extension, title, codeMirrorKey);
	}

	@Override
	protected Integer order() {
		return 300;
	}

	@Override
	public String checkSyntaxErrors(String path, String script) {
		URL url;
		try {
			url = new URL("file", "", path);
			final CompilationUnit unit = new CompilationUnit(CompilerConfiguration.DEFAULT, new CodeSource(url,
					(java.security.cert.Certificate[]) null), null);
			unit.addSource(path, script);
			unit.compile(Phases.CONVERSION);
		} catch (MalformedURLException e) {
			noOp();
		} catch (CompilationFailedException ce) {
			return ce.getMessage();
		}
		return null;
	}

	@Override
	public Integer displayOrder() {
		return 200;
	}

	/**
	 * Get the default quick test file.
	 *
	 * @param basePath base path
	 * @return quick test file
	 */
	public FileEntry getDefaultQuickTestFilePath(String basePath) {
		FileEntry fileEntry = new FileEntry();
		fileEntry.setPath(PathUtils.join(basePath, "TestRunner." + getExtension()));
		return fileEntry;
	}
}
