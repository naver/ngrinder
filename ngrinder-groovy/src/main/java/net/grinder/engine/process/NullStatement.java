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
package net.grinder.engine.process;

import org.junit.runners.model.Statement;

/**
 * Empty statement which does nothing.
 * 
 * This class is get by {@link #getInstance()} method.
 * 
 * @author JunHo Yoon
 * @since 3.2
 */
public class NullStatement extends Statement {
	private static NullStatement instance = new NullStatement();

	NullStatement() {

	}

	@Override
	public void evaluate() throws Throwable {
		// Do Nothing
	}

	/**
	 * Get the instance.
	 * 
	 * @return NullStatement instance
	 */
	public static Statement getInstance() {
		return instance;
	}
}
