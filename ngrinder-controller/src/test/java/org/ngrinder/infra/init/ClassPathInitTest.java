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
package org.ngrinder.infra.init;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ClassPathInitTest {
	@Test
	public void testClassPathInit() {
		ClassPathInit classPathInit = new ClassPathInit();
		classPathInit.init();
		String agent = getAgent();
		assertThat(agent, not(containsString("javadoc")));
		assertThat(agent, not(containsString("sources")));
	}

	public String getAgent() {
		final String systemClasspath = System.getProperty("java.class.path");
		if (systemClasspath != null) {
			for (String pathEntry : systemClasspath.split(File.pathSeparator)) {
				final File f = new File(pathEntry).getParentFile();
				final File parentFile = f != null ? f : new File(".");

				final Collection<File> children = FileUtils.listFiles(parentFile, null, false);

				for (File candidate : children) {
					final String name = candidate.getName();
					if (name.startsWith("grinder-dcr-agent") && name.endsWith(".jar")) {
						return name;
					}
				}
			}
		}
		return null;
	}
}
