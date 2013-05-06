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

import java.util.List;

import net.grinder.util.AbstractGrinderClassPathProcessor;

/**
 * Jython Class Path processor.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
public class JythonGrinderClassPathProcessor extends AbstractGrinderClassPathProcessor {

	/**
	 * Constructor.
	 */
	public JythonGrinderClassPathProcessor() {
		super();
	}

	@Override
	protected void initMore() {
		List<String> usefulJarList = getUsefulJarList();
		usefulJarList.add("jython-2.5");
		usefulJarList.add("jython-standalone-2.5");
		usefulJarList.add("commons-io");
		usefulJarList.add("commons-lang");
	}

}
