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
package net.grinder.lang.groovy;

import groovy.lang.GroovyClassLoader;
import net.grinder.lang.AbstractLanguageHandler;
import net.grinder.util.AbstractGrinderClassPathProcessor;

/**
 * Groovy Language Handler.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
public class GroovyHandler extends AbstractLanguageHandler {
	private GroovyGrinderClassPathProcessor classPathProcessor = new GroovyGrinderClassPathProcessor();

	/**
	 * Constructor.
	 */
	public GroovyHandler() {
		super("groovy", "Groovy", "groovy");
	}

	@Override
	public AbstractGrinderClassPathProcessor getClassPathProcesssor() {
		return classPathProcessor;
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
