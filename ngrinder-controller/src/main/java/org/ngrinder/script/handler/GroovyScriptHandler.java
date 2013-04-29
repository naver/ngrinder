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

import groovy.lang.GroovyClassLoader;

import org.springframework.stereotype.Component;

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
		super("groovy", "groovy", "Groovy Single Script", "groovy");
	}

	/**
	 * Constructor replacing the key and title.
	 * 
	 * @param key
	 *            key
	 * @param title
	 *            title
	 */
	public GroovyScriptHandler(String key, String title) {
		super(key, "groovy", title, "groovy");
	}

	@Override
	protected Integer order() {
		return 300;
	}

	@Override
	public String checkSyntaxErrors(String script) {
		GroovyClassLoader loader = new GroovyClassLoader();
		try {
			loader.parseClass(script);
		} catch (Exception e) {
			return e.getMessage();
		}
		return null;
	}
}
