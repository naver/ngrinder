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
package net.grinder.scriptengine.groovy;

import net.grinder.scriptengine.exception.AbstractExceptionProcessor;

/**
 * Groovy Exception Processor.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
public class GroovyExceptionProcessor extends AbstractExceptionProcessor {
	private static final String[] NGRINDER_GROOVY_PACKAGE = System
					.getProperty("groovy.sanitized.stacktraces",
									"groovy.," + "org.codehaus.groovy.," + "java.," + "javax.," + "sun.,"
													+ "gjdk.groovy.," + "org.junit.,"
													+ "net.grinder.scriptengine.exception.AbstractExceptionProcessor.,")
					.split("(\\s|,)+");

	@Override
	protected String[] getUninterestingPackages() {
		return NGRINDER_GROOVY_PACKAGE;
	}

	@Override
	protected String[] getInterestingPackages() {
		return new String[] {};
	}

}
