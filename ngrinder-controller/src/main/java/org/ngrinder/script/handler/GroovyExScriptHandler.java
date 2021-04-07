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

import org.springframework.stereotype.Component;

/**
 * Groovy {@link ScriptHandler} using experimental HTTP client template.
 *
 * @since 3.5.5
 */
@Component
public class GroovyExScriptHandler extends GroovyScriptHandler {

	/**
	 * Constructor.
	 */
	public GroovyExScriptHandler() {
		this("groovy_ex", "groovy", "Groovy (Experimental)", "groovy", true);
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
	public GroovyExScriptHandler(String key, String extension, String title, String codeMirrorKey, boolean creatable) {
		super(key, extension, title, codeMirrorKey, creatable);
	}

	@Override
	protected Integer order() {
		return 310;
	}

	@Override
	public Integer displayOrder() {
		return 210;
	}

	@Override
	public String getScriptTemplateName() {
		return "basic_template_groovy_ex.ftl";
	}
}
