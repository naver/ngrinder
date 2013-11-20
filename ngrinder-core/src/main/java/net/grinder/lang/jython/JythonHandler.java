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
package net.grinder.lang.jython;

import net.grinder.lang.AbstractLanguageHandler;
import net.grinder.util.AbstractGrinderClassPathProcessor;

/**
 * Jython Language Handler.
 * 
 * @author JunHo Yoon
 * @since 3.1
 */
public class JythonHandler extends AbstractLanguageHandler {

	private JythonGrinderClassPathProcessor classPathProcessor = new JythonGrinderClassPathProcessor();

	/**
	 * JythonHandler constructor.
	 */
	public JythonHandler() {
		super("py", "Jython");
	}

	@Override
	public AbstractGrinderClassPathProcessor getClassPathProcessor() {
		return classPathProcessor;
	}

}
