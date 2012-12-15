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
package org.ngrinder;

import java.io.File;

import org.junit.Before;

/**
 * TestBase for sigar lib path
 * 
 * @author JunHo Yoon
 */
public class SigarTestBase {

	@Before
	public void setupSigarLibPath() {
		System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator
						+ new File("./native_lib").getAbsolutePath());
	}
}
